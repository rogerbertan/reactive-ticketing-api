create table events(
    id bigserial not null,
    type varchar(30) not null,
    name varchar(100) not null,
    date date,
    description varchar(200) not null,

    primary key(id)
);