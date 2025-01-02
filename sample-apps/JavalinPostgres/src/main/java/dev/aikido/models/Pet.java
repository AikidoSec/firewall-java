package dev.aikido.models;
/*
Creating this table using SQL :
CREATE TABLE public.pets
(
    pet_id integer NOT NULL GENERATED ALWAYS AS IDENTITY (START 1 INCREMENT 1 ),
    name character varying(255) NOT NULL,
    owner character varying(255) NOT NULL,
    CONSTRAINT pet_pkey PRIMARY KEY (pet_id)
)
 */
public record Pet(Integer pet_id, String name, String owner) {}