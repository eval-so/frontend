# --- Create an 'active users' group, too.

# ---!Ups
insert into groups(name, description) values(
  'users', 'Active Users');

# ---!Downs
delete from groups where name='users';
