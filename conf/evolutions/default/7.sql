# --- Create a groups table, and a table to link users and groups.
# --- Also create an 'administrators' group, which always has ID 1.

# ---!Ups
create table groups(
  id serial not null primary key,
  name varchar(255) not null,
  description varchar(255) not null
);

insert into groups(name, description) values(
  'administrators', 'Breakpoint Administrators');

create index groups_name_idx on groups (name);

create table group_users(
  id bigserial not null primary key,
  user_id bigint not null references users (id),
  group_id bigint not null references groups (id)
);

create index groups_users_user_idx on group_users (user_id);

# ---!Downs
drop table if exists groups cascade;
drop table if exists group_users cascade;
