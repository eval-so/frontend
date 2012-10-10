# ---!Ups
drop index application_users_idx;
alter table application_users drop column secret_key;
alter table users add column secret_key varchar(36) not null default '';
create index users_secret_key_idx on users (username, secret_key);
alter table applications add column allow_anonymous_evals boolean not null default false;

# ---!Downs
alter table application_users add column secret_key varchar(36) not null;
alter table users drop column secret_key;
create index application_users_idx on application_users (user_id, secret_key);
alter table applications drop column allow_anonymous_evals;
