

INSERT INTO QUESTIONS VALUES ('1', 'ФИО', 'text', null)
INSERT INTO QUESTIONS VALUES ('2', 'Собакоориентированы ли вы?', 'multiple_choice', '[{"label": "Да", "isCorrect":true}, {"label": "Нет", "isCorrect":false}]')


-- docker run --name postgresql -e POSTGRESQL_USERNAME=myuser -e POSTGRESQL_PASSWORD=mypass -e POSTGRESQL_DATABASE=postgres bitnami/postgresql:latest
--
--
-- docker run -p 5432:5432 -e ALLOW_EMPTY_PASSWORD=yes --name postgresql bitnami/postgresql:latest
--
--
-- create database postgres;
-- create user myuser with password 'mypass';
-- grant all privileges on database postgres to myuser;

-- scp app.jar piedpiper@34.90.231.206:/home/piedpiper