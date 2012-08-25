# ---!Ups
alter table users add column confirmed_at timestamp;
alter table users add column confirmation_token varchar(36) not null;

# ---!Downs
alter table users drop column confirmed_at;
alter table users drop column confirmation_token;
