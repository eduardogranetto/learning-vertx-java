CREATE TABLE public.customers (
	id uuid NOT NULL,
	name text NOT NULL,
	CONSTRAINT customers_pk PRIMARY KEY (id)
);
