CREATE OR REPLACE FUNCTION select_entropy(gifts FLOAT[])
  RETURNS FLOAT[] AS $$
DECLARE
  question RECORD;
  result RECORD;
  entropy FLOAT[];
  yes_vector FLOAT[];
  no_vector FLOAT[];
  max_yes FLOAT;
  max_no FLOAT;
  p_yes FLOAT;
  h_yes FLOAT;
  p_no FLOAT;
  h_no FLOAT;
BEGIN
  FOR question IN SELECT id FROM "Questions"
  LOOP
    p_yes := 0;
    p_no := 0;
    FOR result IN SELECT gift_id, pos_yes_count AS yes_count, pos_no_count AS no_count
                  FROM "Answers" WHERE question_id = question.id ORDER BY gift_id ASC
    LOOP
      yes_vector[result.gift_id] := gifts[result.gift_id] * result.yes_count / (result.yes_count + result.no_count);
      no_vector[result.gift_id] := gifts[result.gift_id] * result.no_count / (result.yes_count + result.no_count);
      p_yes := p_yes + yes_vector[result.gift_id];
      p_no := p_no + no_vector[result.gift_id];
    END LOOP;

    max_yes := 0;
    max_no := 0;
    FOR i IN 1..array_length(yes_vector, 1)
    LOOP
      IF yes_vector[i] > max_yes THEN
        max_yes := yes_vector[i];
      END IF;
      IF no_vector[i] > max_no THEN
        max_no := no_vector[i];
      END IF;
    END LOOP;

    h_yes := 0;
    h_no := 0;
    FOR i IN 1..array_length(yes_vector, 1)
    LOOP
      yes_vector[i] = yes_vector[i] / max_yes;
      h_yes = h_yes - yes_vector[i] * log(yes_vector[i]);
      no_vector[i] = no_vector[i] / max_no;
      h_no = h_no - no_vector[i] * log(no_vector[i]);
    END LOOP;

    entropy[question.id] = p_yes * h_yes + p_no * h_no;
  END LOOP;

  RETURN entropy;
END;
$$ LANGUAGE plpgsql;