create sequence hibernate_sequence start 1 increment 1
create table FridgeOrder (id varchar(255) not null, customerId varchar(255), partsOrdered boolean not null, status int4, primary key (id))
create table FridgeOrder_OrderItem (FridgeOrder_id varchar(255) not null, orderItems_id varchar(255) not null, primary key (FridgeOrder_id, orderItems_id))
create table OrderItem (id varchar(255) not null, quantity int4 not null, product_id int4, primary key (id))
create table Part (id varchar(255) not null, cost float8 not null, supplier int4, primary key (id))
create table Product (id int4 not null, name varchar(255), productionTime int4 not null, primary key (id))
create table Product_ProductPart (Product_id int4 not null, productParts_id varchar(255) not null, primary key (Product_id, productParts_id))
create table ProductPart (id varchar(255) not null, quantity int4 not null, part_id varchar(255), primary key (id))
alter table if exists FridgeOrder_OrderItem add constraint UK_kp8vrq7nwa5yllmgv3rftrj6o unique (orderItems_id)
alter table if exists Product_ProductPart add constraint UK_709wurouqqp1njw8jqkc5x652 unique (productParts_id)
alter table if exists FridgeOrder_OrderItem add constraint FKm8mxjwjm3lprc2ut2b0w4p57i foreign key (orderItems_id) references OrderItem
alter table if exists FridgeOrder_OrderItem add constraint FKthmac3wwttsyvgv2ctk5s1has foreign key (FridgeOrder_id) references FridgeOrder
alter table if exists OrderItem add constraint FKg23j1vs750x8lkx2aesfk6n2 foreign key (product_id) references Product
alter table if exists Product_ProductPart add constraint FKr6dy7o81v0f4gm5ukbcqlys8y foreign key (productParts_id) references ProductPart
alter table if exists Product_ProductPart add constraint FKtj5ihl5hjaskjpr9qpyjn7ybi foreign key (Product_id) references Product
alter table if exists ProductPart add constraint FKov8rf8ux1foswob4ubevd8267 foreign key (part_id) references Part

INSERT INTO public.product (id, name, productiontime) VALUES (1, 'Produkt 1', 2);
INSERT INTO public.product (id, name, productiontime) VALUES (2, 'Produkt 2', 4);
INSERT INTO public.product (id, name, productiontime) VALUES (3, 'Produkt 3', 3);
INSERT INTO public.product (id, name, productiontime) VALUES (4, 'Produkt 4', 6);
INSERT INTO public.product (id, name, productiontime) VALUES (5, 'Produkt 5', 5);

INSERT INTO public.part (id, cost, supplier) VALUES ('4028808972ec265e0172ec26612b0000', 10, 1);
INSERT INTO public.part (id, cost, supplier) VALUES ('4028808972ec265e0172ec26612b0002', 5, 0);
INSERT INTO public.part (id, cost, supplier) VALUES ('4028808972ec265e0172ec26612b0004', 1, 0);
INSERT INTO public.part (id, cost, supplier) VALUES ('4028808972ec265e0172ec26614a0006', 10, 1);
INSERT INTO public.part (id, cost, supplier) VALUES ('4028808972ec265e0172ec26614a0008', 5, 0);
INSERT INTO public.part (id, cost, supplier) VALUES ('4028808972ec265e0172ec26614b000a', 1, 0);
INSERT INTO public.part (id, cost, supplier) VALUES ('4028808972ec265e0172ec266159000c', 4, 1);
INSERT INTO public.part (id, cost, supplier) VALUES ('4028808972ec265e0172ec266159000e', 4, 0);
INSERT INTO public.part (id, cost, supplier) VALUES ('4028808972ec265e0172ec2661650010', 2, 0);
INSERT INTO public.part (id, cost, supplier) VALUES ('4028808972ec265e0172ec2661650012', 2, 1);
INSERT INTO public.part (id, cost, supplier) VALUES ('4028808972ec265e0172ec2661700014', 3, 1);
INSERT INTO public.part (id, cost, supplier) VALUES ('4028808972ec265e0172ec2661700016', 10, 1);
INSERT INTO public.part (id, cost, supplier) VALUES ('4028808972ec265e0172ec2661710018', 7, 1);
INSERT INTO public.part (id, cost, supplier) VALUES ('4028808972ec265e0172ec266171001a', 6, 0);

INSERT INTO public.productpart (id, quantity, part_id) VALUES ('4028808972ec265e0172ec26612b0001', 1, '4028808972ec265e0172ec26612b0000');
INSERT INTO public.productpart (id, quantity, part_id) VALUES ('4028808972ec265e0172ec26612b0003', 2, '4028808972ec265e0172ec26612b0002');
INSERT INTO public.productpart (id, quantity, part_id) VALUES ('4028808972ec265e0172ec26612b0005', 2, '4028808972ec265e0172ec26612b0004');
INSERT INTO public.productpart (id, quantity, part_id) VALUES ('4028808972ec265e0172ec26614a0007', 1, '4028808972ec265e0172ec26614a0006');
INSERT INTO public.productpart (id, quantity, part_id) VALUES ('4028808972ec265e0172ec26614b0009', 2, '4028808972ec265e0172ec26614a0008');
INSERT INTO public.productpart (id, quantity, part_id) VALUES ('4028808972ec265e0172ec26614b000b', 2, '4028808972ec265e0172ec26614b000a');
INSERT INTO public.productpart (id, quantity, part_id) VALUES ('4028808972ec265e0172ec266159000d', 2, '4028808972ec265e0172ec266159000c');
INSERT INTO public.productpart (id, quantity, part_id) VALUES ('4028808972ec265e0172ec266159000f', 1, '4028808972ec265e0172ec266159000e');
INSERT INTO public.productpart (id, quantity, part_id) VALUES ('4028808972ec265e0172ec2661650011', 1, '4028808972ec265e0172ec2661650010');
INSERT INTO public.productpart (id, quantity, part_id) VALUES ('4028808972ec265e0172ec2661650013', 4, '4028808972ec265e0172ec2661650012');
INSERT INTO public.productpart (id, quantity, part_id) VALUES ('4028808972ec265e0172ec2661700015', 1, '4028808972ec265e0172ec2661700014');
INSERT INTO public.productpart (id, quantity, part_id) VALUES ('4028808972ec265e0172ec2661700017', 1, '4028808972ec265e0172ec2661700016');
INSERT INTO public.productpart (id, quantity, part_id) VALUES ('4028808972ec265e0172ec2661710019', 2, '4028808972ec265e0172ec2661710018');
INSERT INTO public.productpart (id, quantity, part_id) VALUES ('4028808972ec265e0172ec266171001b', 1, '4028808972ec265e0172ec266171001a');

INSERT INTO public.product_productpart (product_id, productparts_id) VALUES (1, '4028808972ec265e0172ec26612b0003');
INSERT INTO public.product_productpart (product_id, productparts_id) VALUES (1, '4028808972ec265e0172ec26612b0005');
INSERT INTO public.product_productpart (product_id, productparts_id) VALUES (1, '4028808972ec265e0172ec26612b0001');
INSERT INTO public.product_productpart (product_id, productparts_id) VALUES (2, '4028808972ec265e0172ec26614b000b');
INSERT INTO public.product_productpart (product_id, productparts_id) VALUES (2, '4028808972ec265e0172ec26614b0009');
INSERT INTO public.product_productpart (product_id, productparts_id) VALUES (2, '4028808972ec265e0172ec26614a0007');
INSERT INTO public.product_productpart (product_id, productparts_id) VALUES (3, '4028808972ec265e0172ec266159000d');
INSERT INTO public.product_productpart (product_id, productparts_id) VALUES (3, '4028808972ec265e0172ec266159000f');
INSERT INTO public.product_productpart (product_id, productparts_id) VALUES (4, '4028808972ec265e0172ec2661650011');
INSERT INTO public.product_productpart (product_id, productparts_id) VALUES (4, '4028808972ec265e0172ec2661650013');
INSERT INTO public.product_productpart (product_id, productparts_id) VALUES (5, '4028808972ec265e0172ec2661710019');
INSERT INTO public.product_productpart (product_id, productparts_id) VALUES (5, '4028808972ec265e0172ec266171001b');
INSERT INTO public.product_productpart (product_id, productparts_id) VALUES (5, '4028808972ec265e0172ec2661700015');
INSERT INTO public.product_productpart (product_id, productparts_id) VALUES (5, '4028808972ec265e0172ec2661700017');