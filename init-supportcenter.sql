create table SupportTicket (id varchar(255) not null, closingTime timestamp, creationTime timestamp, customerId varchar(255), isClosed boolean not null, text varchar(5000), completedAt timestamp, primary key (id));