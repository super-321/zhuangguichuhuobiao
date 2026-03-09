create table lists
(
    id integer primary key,
    bar nvarchar(50),
    model nvarchar(50),
    planning int,
    a int,
    b int,
    c int,
    d int,
    e int,
    f int,
    g int,
    h int,
    i int,
    j int,
    order_no nvarchar(50)
);

create table orders
(
	id integer primary key,
	no nvarchar(50)
);

--insert into lists (bar, model, planning, a,b,c,d,e,f,g,h,i,j) values ('69', 'm-69', 100, 10,10,10,10,10,10,10,10,10,10);
--insert into lists (bar, model, planning, a,b,c,d,e,f,g,h,i,j) values ('69', 'm-69', 100, 10,10,10,10,10,10,10,10,10,0);


--
--SELECT * FROM lists;
--
--select * FROM orders;
