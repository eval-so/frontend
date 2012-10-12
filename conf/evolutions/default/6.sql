# --- Make user.name nullable.

# ---!Ups
alter table users alter column name drop not null;

# ---!Downs
alter table users alter column name set not null;
