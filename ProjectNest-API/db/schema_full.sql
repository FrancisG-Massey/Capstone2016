/*********************************************************
 *	Copyright (C) Project Nest NZ (2016)
 *	Unauthorized copying of this file, via any medium is 
 *		strictly prohibited.
 *	Proprietary and confidential
 *	Written by Sam Hunt <s2112201@ipc.ac.nz>, August 2016
 *********************************************************/

-- Nest Database Initial Setup SQL Script


/*
    Database clean setup instructions:

    1. create a login role 'nestnz' ensuring it has createdb permissions
    2. create a database 'nestnz' ensuring it has default schema 'public'
    3. run the sql script below to populate data table definitions,
       adding primary-key sequence tables and foreign key indexes.

*/

--DROP SCHEMA IF EXISTS public CASCADE;
--CREATE SCHEMA public;


-- First create the sequences for the auto-incrememting primary keys

-- Sequence: public.session_session_id_seq

-- DROP SEQUENCE public.session_session_id_seq;

CREATE SEQUENCE public.session_session_id_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1;
ALTER TABLE public.session_session_id_seq
  OWNER TO nestnz;

-- Sequence: public.bait_bait_id_seq

-- DROP SEQUENCE public.bait_bait_id_seq;

CREATE SEQUENCE public.bait_bait_id_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1;
ALTER TABLE public.bait_bait_id_seq
  OWNER TO nestnz;

-- Sequence: public.catch_catch_id_seq

-- DROP SEQUENCE public.catch_catch_id_seq;

CREATE SEQUENCE public.catch_catch_id_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1;
ALTER TABLE public.catch_catch_id_seq
  OWNER TO nestnz;

-- Sequence: public.catch_type_catch_type_id_seq

-- DROP SEQUENCE public.catch_type_catch_type_id_seq;

CREATE SEQUENCE public.catch_type_catch_type_id_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1;
ALTER TABLE public.catch_type_catch_type_id_seq
  OWNER TO nestnz;

-- Sequence: public.region_region_id_seq

-- DROP SEQUENCE public.region_region_id_seq;

CREATE SEQUENCE public.region_region_id_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1;
ALTER TABLE public.region_region_id_seq
  OWNER TO nestnz;

-- Sequence: public.role_role_id_seq

-- DROP SEQUENCE public.role_role_id_seq;

CREATE SEQUENCE public.role_role_id_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1;
ALTER TABLE public.role_role_id_seq
  OWNER TO nestnz;

-- Sequence: public.trap_line_trap_line_id_seq

-- DROP SEQUENCE public.trap_line_trap_line_id_seq;

CREATE SEQUENCE public.trap_line_trap_line_id_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1;
ALTER TABLE public.trap_line_trap_line_id_seq
  OWNER TO nestnz;

-- Sequence: public.trap_line_user_trap_line_user_id_seq

-- DROP SEQUENCE public.trap_line_user_trap_line_user_id_seq;

CREATE SEQUENCE public.trap_line_user_trap_line_user_id_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1;
ALTER TABLE public.trap_line_user_trap_line_user_id_seq
  OWNER TO nestnz;

-- Sequence: public.trap_trap_id_seq

-- DROP SEQUENCE public.trap_trap_id_seq;

CREATE SEQUENCE public.trap_trap_id_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1;
ALTER TABLE public.trap_trap_id_seq
  OWNER TO nestnz;

-- Sequence: public.trap_type_trap_type_id_seq

-- DROP SEQUENCE public.trap_type_trap_type_id_seq;

CREATE SEQUENCE public.trap_type_trap_type_id_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1;
ALTER TABLE public.trap_type_trap_type_id_seq
  OWNER TO nestnz;

-- Sequence: public.users_user_id_seq

-- DROP SEQUENCE public.users_user_id_seq;

CREATE SEQUENCE public.users_user_id_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1;
ALTER TABLE public.users_user_id_seq
  OWNER TO nestnz;



-- Next define the data tables

-- Table: public.region

-- DROP TABLE public.region;

CREATE TABLE public.region
(
  region_id bigint NOT NULL DEFAULT nextval('region_region_id_seq'::regclass),
  region_name text NOT NULL,
  region_doc_id text NOT NULL,
  CONSTRAINT region_pkey PRIMARY KEY (region_id)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE public.region
  OWNER TO nestnz;


-- Table: public.users

-- DROP TABLE public.users;

CREATE TABLE public.users
(
  user_id bigint NOT NULL DEFAULT nextval('users_user_id_seq'::regclass),
  user_name text NOT NULL,
  user_password text NOT NULL,
  user_contact_fullname text,
  user_contact_phone text,
  user_contact_email text,
  user_created_timestamp timestamp without time zone NOT NULL DEFAULT now(),
  user_created_user_id bigint,
  user_is_admin boolean NOT NULL DEFAULT false,
  user_is_inactive boolean NOT NULL DEFAULT false,
  CONSTRAINT users_pkey PRIMARY KEY (user_id),
  -- All usernames must be unique
  CONSTRAINT users_user_name_key UNIQUE (user_name),
  -- Phone numbers must be of length 8-15 inclusive, and contain only numerals
  CONSTRAINT valid_phone CHECK (user_contact_phone IS NULL OR user_contact_phone ~ '^\d{5,14}$'::text),
  -- Usernames must be at least 3 characters long and not contain colons (breaks basic auth)
  CONSTRAINT valid_username CHECK (user_name ~ '^[^:]{3,}$'::text)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE public.users
  OWNER TO nestnz;


-- Table: public.session

-- DROP TABLE public.session;

CREATE TABLE public.session
(
  session_id bigint NOT NULL DEFAULT nextval('session_session_id_seq'::regclass),
  session_user_id bigint NOT NULL,
  session_token text NOT NULL,
  session_created timestamp without time zone NOT NULL DEFAULT now(),
  CONSTRAINT session_pkey PRIMARY KEY (session_id),
  CONSTRAINT session_session_token UNIQUE (session_token),
  CONSTRAINT session_session_user_id_fkey FOREIGN KEY (session_user_id)
      REFERENCES public.users (user_id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT valid_session_token CHECK (session_token ~ '^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$'::text)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE public.session
  OWNER TO nestnz;

-- Index: public.s_suid_idx

-- DROP INDEX public.s_suid_idx;

CREATE INDEX s_suid_idx
  ON public.session
  USING btree
  (session_user_id);



-- Table: public.bait

-- DROP TABLE public.bait;

CREATE TABLE public.bait
(
  bait_id bigint NOT NULL DEFAULT nextval('bait_bait_id_seq'::regclass),
  bait_name text NOT NULL,
  bait_image_filename text,
  bait_note text,
  CONSTRAINT bait_pkey PRIMARY KEY (bait_id)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE public.bait
  OWNER TO nestnz;


-- Table: public.trap_type

-- DROP TABLE public.trap_type;

CREATE TABLE public.trap_type
(
  trap_type_id bigint NOT NULL DEFAULT nextval('trap_type_trap_type_id_seq'::regclass),
  trap_type_name text,
  trap_type_model text,
  trap_type_note text,
  CONSTRAINT trap_type_pkey PRIMARY KEY (trap_type_id),
  CONSTRAINT trap_type_valid_text_identifier CHECK NOT ((trap_type_model IS NULL) AND (trap_type_name IS NULL))
)
WITH (
  OIDS=FALSE
);
ALTER TABLE public.trap_type
  OWNER TO nestnz;


-- Table: public.trap_line

-- DROP TABLE public.trap_line;

CREATE TABLE public.trap_line
(
  trap_line_id bigint NOT NULL DEFAULT nextval('trap_line_trap_line_id_seq'::regclass),
  trap_line_name text NOT NULL,
  trap_line_doc_id text NOT NULL,
  trap_line_region_id bigint NOT NULL,
  trap_line_start_tag text,
  trap_line_end_tag text,
  trap_line_image_filename text,
  CONSTRAINT trap_line_pkey PRIMARY KEY (trap_line_id),
  CONSTRAINT trap_line_trap_line_region_id_fkey FOREIGN KEY (trap_line_region_id)
      REFERENCES public.region (region_id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE RESTRICT
)
WITH (
  OIDS=FALSE
);
ALTER TABLE public.trap_line
  OWNER TO nestnz;

-- Index: public.tl_rid_idx

-- DROP INDEX public.tl_rid_idx;

CREATE INDEX tl_rid_idx
  ON public.trap_line
  USING btree
  (trap_line_region_id);



-- Table: public.role

-- DROP TABLE public.role;

CREATE TABLE public.role
(
  role_id bigint NOT NULL DEFAULT nextval('role_role_id_seq'::regclass),
  role_name text NOT NULL,
  CONSTRAINT role_pkey PRIMARY KEY (role_id)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE public.role
  OWNER TO nestnz;


-- Table: public.catch_type

-- DROP TABLE public.catch_type;

CREATE TABLE public.catch_type
(
  catch_type_id bigint NOT NULL DEFAULT nextval('catch_type_catch_type_id_seq'::regclass),
  catch_type_name text NOT NULL,
  catch_type_image_filename text,
  CONSTRAINT catch_type_pkey PRIMARY KEY (catch_type_id)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE public.catch_type
  OWNER TO nestnz;


-- Table: public.trap_line_user

-- DROP TABLE public.trap_line_user;

CREATE TABLE public.trap_line_user
(
  trap_line_user_id bigint NOT NULL DEFAULT nextval('trap_line_user_trap_line_user_id_seq'::regclass),
  trap_line_user_user_id bigint NOT NULL,
  trap_line_user_trap_line_id bigint NOT NULL,
  trap_line_user_role_id bigint NOT NULL,
  CONSTRAINT trap_line_user_pkey PRIMARY KEY (trap_line_user_id),
  CONSTRAINT trap_line_user_trap_line_user_role_id_fkey FOREIGN KEY (trap_line_user_role_id)
      REFERENCES public.role (role_id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE RESTRICT,
  CONSTRAINT trap_line_user_trap_line_user_trap_line_id_fkey FOREIGN KEY (trap_line_user_trap_line_id)
      REFERENCES public.trap_line (trap_line_id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE RESTRICT,
  CONSTRAINT trap_line_user_trap_line_user_user_id_fkey FOREIGN KEY (trap_line_user_user_id)
      REFERENCES public.users (user_id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE RESTRICT
)
WITH (
  OIDS=FALSE
);
ALTER TABLE public.trap_line_user
  OWNER TO nestnz;

-- Index: public.tlu_rid_idx

-- DROP INDEX public.tlu_rid_idx;

CREATE INDEX tlu_rid_idx
  ON public.trap_line_user
  USING btree
  (trap_line_user_role_id);

-- Index: public.tlu_tlid_idx

-- DROP INDEX public.tlu_tlid_idx;

CREATE INDEX tlu_tlid_idx
  ON public.trap_line_user
  USING btree
  (trap_line_user_trap_line_id);

-- Index: public.tlu_uid_idx

-- DROP INDEX public.tlu_uid_idx;

CREATE INDEX tlu_uid_idx
  ON public.trap_line_user
  USING btree
  (trap_line_user_user_id);



-- Table: public.trap

-- DROP TABLE public.trap;

CREATE TABLE public.trap
(
  trap_id bigint NOT NULL DEFAULT nextval('trap_trap_id_seq'::regclass),
  trap_line_id bigint NOT NULL,
  trap_doc_id text NOT NULL,
  trap_coord_x numeric NOT NULL,
  trap_coord_y numeric NOT NULL,
  trap_type_id bigint NOT NULL,
  trap_status integer NOT NULL DEFAULT 1,
  trap_created timestamp without time zone NOT NULL DEFAULT now(),
  trap_last_reset timestamp without time zone NOT NULL DEFAULT now(),
  trap_bait_id bigint,
  CONSTRAINT trap_pkey PRIMARY KEY (trap_id),
  CONSTRAINT trap_trap_bait_id_fkey FOREIGN KEY (trap_bait_id)
      REFERENCES public.bait (bait_id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE RESTRICT,
  CONSTRAINT trap_trap_line_id_fkey FOREIGN KEY (trap_line_id)
      REFERENCES public.trap_line (trap_line_id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE RESTRICT,
  CONSTRAINT trap_trap_type_id_fkey FOREIGN KEY (trap_type_id)
      REFERENCES public.trap_type (trap_type_id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE RESTRICT
)
WITH (
  OIDS=FALSE
);
ALTER TABLE public.trap
  OWNER TO nestnz;

-- Index: public.t_btid_idx

-- DROP INDEX public.t_btid_idx;

CREATE INDEX t_btid_idx
  ON public.trap
  USING btree
  (trap_bait_id);

-- Index: public.t_tlid_idx

-- DROP INDEX public.t_tlid_idx;

CREATE INDEX t_tlid_idx
  ON public.trap
  USING btree
  (trap_line_id);

-- Index: public.t_ttid_idx

-- DROP INDEX public.t_ttid_idx;

CREATE INDEX t_ttid_idx
  ON public.trap
  USING btree
  (trap_type_id);



-- Table: public.catch

-- DROP TABLE public.catch;

CREATE TABLE public.catch
(
  catch_id bigint NOT NULL DEFAULT nextval('catch_catch_id_seq'::regclass),
  catch_trap_id bigint NOT NULL,
  catch_trap_type_id bigint NOT NULL,
  catch_user_id bigint NOT NULL,
  catch_type_id bigint NOT NULL,
  catch_bait_id bigint NOT NULL,
  catch_note text,
  catch_logged timestamp without time zone NOT NULL DEFAULT now(),
  catch_image_filename text,
  CONSTRAINT catch_pkey PRIMARY KEY (catch_id),
  CONSTRAINT catch_catch_trap_id_fkey FOREIGN KEY (catch_trap_id)
      REFERENCES public.trap (trap_id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE RESTRICT,
  CONSTRAINT catch_catch_trap_type_id_fkey FOREIGN KEY (catch_trap_type_id)
      REFERENCES public.trap_type (trap_type_id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE RESTRICT,      
  CONSTRAINT catch_catch_type_id_fkey FOREIGN KEY (catch_type_id)
      REFERENCES public.catch_type (catch_type_id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE RESTRICT,
  CONSTRAINT catch_catch_user_id_fkey FOREIGN KEY (catch_user_id)
      REFERENCES public.users (user_id) MATCH SIMPLE  
      ON UPDATE CASCADE ON DELETE RESTRICT,
  CONSTRAINT catch_bait_id_fkey FOREIGN KEY (catch_bait_id)
      REFERENCES public.bait (bait_id) MATCH SIMPLE  
      ON UPDATE CASCADE ON DELETE RESTRICT      
)
WITH (
  OIDS=FALSE
);
ALTER TABLE public.catch
  OWNER TO nestnz;

-- Index: public.c_ctrapid_idx

-- DROP INDEX public.c_ctrapid_idx;

CREATE INDEX c_ctrapid_idx
  ON public.catch
  USING btree
  (catch_trap_id);

-- Index: public.c_cttypeid_idx

-- DROP INDEX public.c_cttypeid_idx;

CREATE INDEX c_cttypeid_idx
  ON public.catch
  USING btree
  (catch_trap_type_id);  

-- Index: public.c_ctypeid_idx

-- DROP INDEX public.c_ctypeid_idx;

CREATE INDEX c_ctypeid_idx
  ON public.catch
  USING btree
  (catch_type_id);

-- Index: public.c_cuid_idx

-- DROP INDEX public.c_cuid_idx;

CREATE INDEX c_cuid_idx
  ON public.catch
  USING btree
  (catch_user_id);

-- Index: public.c_cbid_idx

-- DROP INDEX public.c_cbid_idx;

CREATE INDEX c_cbid_idx
  ON public.catch
  USING btree
  (catch_bait_id);
