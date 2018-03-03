drop trigger if exists gift_insert_trg on "Gifts";
drop function if exists insert_gift;

drop trigger if exists question_insert_trg on "Questions";
drop function if exists insert_question;

create function insert_gift()
returns trigger as
$$
declare
    r "Questions"%rowtype;
begin
    for r in (select * from "Questions")
    loop
        insert into "Answers" (gift_id, question_id) values (NEW.id, r.id);
    end loop;
    
    return NEW;
end;
$$
language 'plpgsql';

create trigger gift_insert_trg
after insert on "Gifts"
for each row
execute procedure insert_gift();

create function insert_question()
returns trigger as
$$
declare
    r "Gifts"%rowtype;
begin
    for r in (select * from "Gifts")
    loop
        insert into "Answers" (gift_id, question_id) values (r.id, NEW.id);
    end loop;
    
    return NEW;
end;
$$
language 'plpgsql';

create trigger question_insert_trg
after insert on "Questions"
for each row
execute procedure insert_question();
