-- Create the products table if not present
CREATE TABLE IF NOT EXISTS public.products (
  id        SERIAL PRIMARY KEY,
  name      VARCHAR(40) NOT NULL,
  stock     BIGINT,
  discReader BOOLEAN,
  storage VARCHAR(40) NOT NULL,
  core_count integer NOT NULL
);

DELETE FROM products;

INSERT INTO products (name, stock, discReader, storage, core_count) values ('Playstation 5', 10, true, '854GB', 8);
INSERT INTO products (name, stock, discReader, storage, core_count) values ('Xbox Series X', 10, true, '1000gb', 8);
INSERT INTO products (name, stock, discReader, storage, core_count) values ('Nintendo Switch', 10, false, '16gb', 8);

ALTER TABLE products
    OWNER to "user";

CREATE TABLE IF NOT EXISTS public.pedidos
(
    fecha_solicitud date NOT NULL,
    cliente_id integer NOT NULL,
    nombre_consola character varying(250) COLLATE pg_catalog."default" NOT NULL,
    cantidad integer NOT NULL,
    lector_disco character varying(1) COLLATE pg_catalog."default" NOT NULL,
    cap_almacenamiento integer NOT NULL,
    can_procesadores integer NOT NULL,
    estado character varying(1) COLLATE pg_catalog."default" NOT NULL,
    fecha_entrega date,
    fecha_pago date,
    CONSTRAINT pk_pedidos PRIMARY KEY (fecha_solicitud, cliente_id)
)
WITH (
    OIDS = FALSE
)
TABLESPACE pg_default;

ALTER TABLE public.pedidos
    OWNER to "user";
