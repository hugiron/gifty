CREATE OR REPLACE FUNCTION select_entropy(pos_gifts FLOAT[], neg_gifts FLOAT[])
  RETURNS FLOAT[] AS $$
DECLARE
  question RECORD;
  result RECORD;
  entropy FLOAT[];
  
  pos_yes_vector FLOAT[];
  pos_no_vector FLOAT[];
  pos_max_yes FLOAT;
  pos_max_no FLOAT;
  pos_p_yes FLOAT;
  pos_h_yes FLOAT;
  pos_p_no FLOAT;
  pos_h_no FLOAT;
  neg_yes_vector FLOAT[];
  neg_no_vector FLOAT[];
  neg_max_yes FLOAT;
  neg_max_no FLOAT;
  neg_p_yes FLOAT;
  neg_h_yes FLOAT;
  neg_p_no FLOAT;
  neg_h_no FLOAT;
BEGIN
  FOR question IN SELECT id FROM "Questions"
  LOOP
    pos_p_yes := 0;
    pos_p_no := 0;
    neg_p_yes := 0;
    neg_p_no := 0;
    FOR result IN SELECT gift_id, pos_yes_count, pos_no_count, neg_yes_count, neg_no_count
                  FROM "Answers" WHERE question_id = question.id ORDER BY gift_id ASC
    LOOP
      pos_yes_vector[result.gift_id] := pos_gifts[result.gift_id] * result.pos_yes_count / (result.pos_yes_count + result.pos_no_count);
      pos_no_vector[result.gift_id] := pos_gifts[result.gift_id] * result.pos_no_count / (result.pos_yes_count + result.pos_no_count);
      neg_yes_vector[result.gift_id] := neg_gifts[result.gift_id] * result.neg_yes_count / (result.neg_yes_count + result.neg_no_count);
      neg_no_vector[result.gift_id] := neg_gifts[result.gift_id] * result.neg_no_count / (result.neg_yes_count + result.neg_no_count);
      pos_p_yes := pos_p_yes + pos_yes_vector[result.gift_id];
      pos_p_no := pos_p_no + pos_no_vector[result.gift_id];
      neg_p_yes := neg_p_yes + neg_yes_vector[result.gift_id];
      neg_p_no := neg_p_no + neg_no_vector[result.gift_id];
    END LOOP;

    pos_max_yes := 0;
    pos_max_no := 0;
    neg_max_yes := 0;
    neg_max_no := 0;
    FOR i IN 1..array_length(pos_yes_vector, 1)
    LOOP
      IF pos_yes_vector[i] > pos_max_yes THEN
        pos_max_yes := pos_yes_vector[i];
      END IF;
      IF pos_no_vector[i] > pos_max_no THEN
        pos_max_no := pos_no_vector[i];
      END IF;
      IF neg_yes_vector[i] > neg_max_yes THEN
        neg_max_yes := neg_yes_vector[i];
      END IF;
      IF neg_no_vector[i] > neg_max_no THEN
        neg_max_no := neg_no_vector[i];
      END IF;
    END LOOP;

    pos_h_yes := 0;
    pos_h_no := 0;
    neg_h_yes := 0;
    neg_h_no := 0;
    FOR i IN 1..array_length(pos_yes_vector, 1)
    LOOP
      pos_yes_vector[i] = pos_yes_vector[i] / pos_max_yes;
      pos_h_yes = pos_h_yes - pos_yes_vector[i] * log(pos_yes_vector[i] + 1e-8);
      pos_no_vector[i] = pos_no_vector[i] / pos_max_no;
      pos_h_no = pos_h_no - pos_no_vector[i] * log(pos_no_vector[i] + 1e-8);
      neg_yes_vector[i] = neg_yes_vector[i] / neg_max_yes;
      neg_h_yes = neg_h_yes - neg_yes_vector[i] * log(neg_yes_vector[i] + 1e-8);
      neg_no_vector[i] = neg_no_vector[i] / neg_max_no;
      neg_h_no = neg_h_no - neg_no_vector[i] * log(neg_no_vector[i] + 1e-8);
    END LOOP;

    entropy[question.id] = pos_p_yes * pos_h_yes + pos_p_no * pos_h_no;
  END LOOP;

  RETURN entropy;
END;
$$ LANGUAGE plpgsql;