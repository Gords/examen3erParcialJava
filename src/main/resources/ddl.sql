CREATE TABLE IF NOT EXISTS public.products (
  id        SERIAL PRIMARY KEY,
  name      VARCHAR(40) NOT NULL,
  stock     BIGINT,
  discReader VARCHAR(40) NOT NULL,
  storage BIGINT,
  coreCount BIGINT NOT NULL
);

INSERT INTO public.products (name, stock, discReader, storage, coreCount) values ('Playstation 5', 10, 'si', 854, 8);
INSERT INTO public.products (name, stock, discReader, storage, coreCount) values ('Xbox Series X', 10, 'si', 1000, 8);
INSERT INTO public.products (name, stock, discReader, storage, coreCount) values ('Nintendo Switch', 10, 'si', 16, 8);

ALTER TABLE public.products
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
