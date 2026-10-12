create table events(
    id uuid not null default gen_random_uuid(),
    type varchar(30) not null,
    name varchar(100) not null,
    date date,
    description varchar(200) not null,

    primary key(id)
);