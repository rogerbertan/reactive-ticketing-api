create table tickets(
    id uuid not null default gen_random_uuid(),
    event_id uuid not null,
    type varchar(30) not null,
    price decimal(10, 2) not null,
    quantity int not null,

    primary key(id),
    foreign key(event_id) references events(id) on delete cascade
);