# ---!Ups
create table servers(
  id serial not null primary key,
  hostname varchar(255) not null,
  arch varchar(10) not null,
  enabled boolean not null default false
);

create index servers_idx on servers (hostname, enabled);

# ---!Downs
drop table if exists servers cascade;
