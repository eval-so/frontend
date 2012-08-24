# ---!Ups
create table users(
  id bigserial not null primary key,
  username varchar(255) not null,
  password varchar(64) not null,
  salt varchar(36) not null,
  name varchar(255) not null,
  email varchar(255) not null,
  registered_at timestamp not null default current_timestamp
);

create index users_idx on users (username, password, salt);

create table applications(
  id bigserial not null primary key,
  name varchar(255) not null,
  api_id varchar(36) not null,
  created_at timestamp not null default current_timestamp
);

create index applications_idx on applications (api_id);

create table application_users(
  id bigserial not null primary key,
  user_id bigint not null references users (id),
  application_id bigint not null references applications (id),
  secret_key varchar(36) not null,
  owner boolean not null default false,
  granted_at timestamp not null default current_timestamp
);

create index application_users_user_id_idx on application_users (user_id);
create index application_users_idx on application_users (user_id, secret_key);

# ---!Downs
drop table if exists users cascade;
drop table if exists applications cascade;
drop table if exists application_users cascade;
