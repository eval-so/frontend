# Add a description field to the application table.

# ---!Ups
alter table applications add column description varchar(255);

# ---!Downs
alter table applications drop column description;
