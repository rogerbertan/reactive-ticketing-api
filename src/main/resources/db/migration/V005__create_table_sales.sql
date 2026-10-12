create table sales(
    id uuid not null default gen_random_uuid(),
    ticket_id uuid not null,
    quantity int not null,
    total_price decimal(10,2) not null,

    primary key(id),
    foreign key(ticket_id) references tickets(id) on delete cascade
);