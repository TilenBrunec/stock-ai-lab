create table instrument (
                            id serial primary key,
                            ticker varchar(20) not null unique,
                            name varchar(200) not null
);

create table price (
                       instrument_id integer not null references instrument(id),
                       trade_date date not null,
                       adj_close numeric(14,4) not null,
                       primary key (instrument_id, trade_date)
);

alter table instrument enable row level security;
alter table price enable row level security;