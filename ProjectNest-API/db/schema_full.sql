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
--ALTER SCHEMA public OWNER TO nestnz;


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


-- Sequence: public.catchtype_catchtype_id_seq
-- DROP SEQUENCE public.catchtype_catchtype_id_seq;

CREATE SEQUENCE public.catchtype_catchtype_id_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1;
ALTER TABLE public.catchtype_catchtype_id_seq
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


-- Sequence: public.trapline_trapline_id_seq
-- DROP SEQUENCE public.trapline_trapline_id_seq;

CREATE SEQUENCE public.trapline_trapline_id_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1;
ALTER TABLE public.trapline_trapline_id_seq
  OWNER TO nestnz;


-- Sequence: public.traplineuser_traplineuser_id_seq
-- DROP SEQUENCE public.traplineuser_traplineuser_id_seq;

CREATE SEQUENCE public.traplineuser_traplineuser_id_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1;
ALTER TABLE public.traplineuser_traplineuser_id_seq
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


-- Sequence: public.traptype_traptype_id_seq
-- DROP SEQUENCE public.traptype_traptype_id_seq;

CREATE SEQUENCE public.traptype_traptype_id_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1;
ALTER TABLE public.traptype_traptype_id_seq
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
  user_contactfullname text,
  user_contactphone text,
  user_contactemail text,
  user_createdtimestamp timestamp without time zone NOT NULL DEFAULT now()::timestamp,
  user_createduserid bigint,
  user_isadmin boolean NOT NULL DEFAULT false,
  user_isinactive boolean NOT NULL DEFAULT false,
  CONSTRAINT users_pkey PRIMARY KEY (user_id),
  -- All usernames must be unique
  CONSTRAINT users_user_name_key UNIQUE (user_name),
  -- Phone numbers must be of length 8-15 inclusive, and contain only numerals
  CONSTRAINT valid_phone CHECK (user_contactphone IS NULL OR user_contactphone ~ '^\d{5,14}$'::text),
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
  session_userid bigint NOT NULL,
  session_token text NOT NULL,
  session_createdtimestamp timestamp without time zone NOT NULL DEFAULT now()::timestamp,
  CONSTRAINT session_pkey PRIMARY KEY (session_id),
  CONSTRAINT session_session_token UNIQUE (session_token),
  CONSTRAINT session_session_userid_fkey FOREIGN KEY (session_userid)
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
  (session_userid);


-- Index: public.s_suid_idx
-- DROP INDEX public.s_suid_idx;

CREATE INDEX s_sst_idx
  ON public.session
  USING btree
  (session_token);


-- Table: public.bait
-- DROP TABLE public.bait;

CREATE TABLE public.bait
(
  bait_id bigint NOT NULL DEFAULT nextval('bait_bait_id_seq'::regclass),
  bait_name text NOT NULL,
  bait_imagefilename text,
  bait_note text,
  CONSTRAINT bait_pkey PRIMARY KEY (bait_id)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE public.bait
  OWNER TO nestnz;


-- Table: public.traptype
-- DROP TABLE public.traptype;

CREATE TABLE public.traptype
(
  traptype_id bigint NOT NULL DEFAULT nextval('traptype_traptype_id_seq'::regclass),
  traptype_name text,
  traptype_model text,
  traptype_note text,
  CONSTRAINT traptype_pkey PRIMARY KEY (traptype_id),
  CONSTRAINT traptype_valid_text_identifier CHECK (NOT ((traptype_model IS NULL) AND (traptype_name IS NULL)))
)
WITH (
  OIDS=FALSE
);
ALTER TABLE public.traptype
  OWNER TO nestnz;


-- Table: public.trapline
-- DROP TABLE public.trapline;

CREATE TABLE public.trapline
(
  trapline_id bigint NOT NULL DEFAULT nextval('trapline_trapline_id_seq'::regclass),
  trapline_name text NOT NULL,
  trapline_regionid bigint NOT NULL,
  trapline_starttag text,
  trapline_endtag text,
  trapline_imagefilename text,
  CONSTRAINT trapline_pkey PRIMARY KEY (trapline_id),
  CONSTRAINT trapline_trapline_regionid_fkey FOREIGN KEY (trapline_regionid)
      REFERENCES public.region (region_id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE RESTRICT
)
WITH (
  OIDS=FALSE
);
ALTER TABLE public.trapline
  OWNER TO nestnz;


-- Index: public.tl_rid_idx
-- DROP INDEX public.tl_rid_idx;

CREATE INDEX tl_rid_idx
  ON public.trapline
  USING btree
  (trapline_regionid);


-- Table: public.catchtype
-- DROP TABLE public.catchtype;

CREATE TABLE public.catchtype
(
  catchtype_id bigint NOT NULL DEFAULT nextval('catchtype_catchtype_id_seq'::regclass),
  catchtype_name text NOT NULL,
  catchtype_imagefilename text,
  CONSTRAINT catchtype_pkey PRIMARY KEY (catchtype_id)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE public.catchtype
  OWNER TO nestnz;


-- Table: public.trapline_user
-- DROP TABLE public.trapline_user;

CREATE TABLE public.traplineuser
(
  traplineuser_id bigint NOT NULL DEFAULT nextval('traplineuser_traplineuser_id_seq'::regclass),
  traplineuser_userid bigint NOT NULL,
  traplineuser_traplineid bigint NOT NULL,
  traplineuser_isadmin boolean NOT NULL,
  CONSTRAINT traplineuser_pkey PRIMARY KEY (traplineuser_id),
  CONSTRAINT traplineuser_traplineuser_traplineid_fkey FOREIGN KEY (traplineuser_traplineid)
      REFERENCES public.trapline (trapline_id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE RESTRICT,
  CONSTRAINT traplineuser_traplineuser_userid_fkey FOREIGN KEY (traplineuser_userid)
      REFERENCES public.users (user_id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE RESTRICT
)
WITH (
  OIDS=FALSE
);
ALTER TABLE public.traplineuser
  OWNER TO nestnz;


-- Index: public.tlu_tlid_idx
-- DROP INDEX public.tlu_tlid_idx;

CREATE INDEX tlu_tlid_idx
  ON public.traplineuser
  USING btree
  (traplineuser_traplineid);


-- Index: public.tlu_uid_idx
-- DROP INDEX public.tlu_uid_idx;

CREATE INDEX tlu_uid_idx
  ON public.traplineuser
  USING btree
  (traplineuser_userid);



-- Table: public.trap
-- DROP TABLE public.trap;

CREATE TABLE public.trap
(
  trap_id bigint NOT NULL DEFAULT nextval('trap_trap_id_seq'::regclass),
  trap_traplineid bigint NOT NULL,
  trap_number bigint NOT NULL,
  trap_coordx numeric NOT NULL,
  trap_coordy numeric NOT NULL,
  trap_traptypeid bigint NOT NULL,
  trap_status integer NOT NULL DEFAULT 1,
  trap_createdtimestamp timestamp without time zone NOT NULL DEFAULT now()::timestamp,
  trap_lastresettimestamp timestamp without time zone NOT NULL DEFAULT now()::timestamp,
  trap_baitid bigint,
  CONSTRAINT trap_pkey PRIMARY KEY (trap_id),
  CONSTRAINT trap_trap_baitid_fkey FOREIGN KEY (trap_baitid)
      REFERENCES public.bait (bait_id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE RESTRICT,
  CONSTRAINT trap_traplineid_fkey FOREIGN KEY (trap_traplineid)
      REFERENCES public.trapline (trapline_id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE RESTRICT,
  CONSTRAINT trap_traptype_id_fkey FOREIGN KEY (trap_traptypeid)
      REFERENCES public.traptype (traptype_id) MATCH SIMPLE
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
  (trap_baitid);


-- Index: public.t_tlid_idx
-- DROP INDEX public.t_tlid_idx;

CREATE INDEX t_tlid_idx
  ON public.trap
  USING btree
  (trap_traplineid);


-- Index: public.t_ttid_idx
-- DROP INDEX public.t_ttid_idx;

CREATE INDEX t_ttid_idx
  ON public.trap
  USING btree
  (trap_traptypeid);



-- Table: public.catch
-- DROP TABLE public.catch;

CREATE TABLE public.catch
(
  catch_id bigint NOT NULL DEFAULT nextval('catch_catch_id_seq'::regclass),
  catch_trapid bigint NOT NULL,
  catch_traptypeid bigint NOT NULL,
  catch_userid bigint NOT NULL,
  catch_catchtypeid bigint NOT NULL,
  catch_baitid bigint NOT NULL,
  catch_note text,
  catch_loggedtimestamp timestamp without time zone NOT NULL DEFAULT now()::timestamp,
  catch_imagefilename text,
  CONSTRAINT catch_pkey PRIMARY KEY (catch_id),
  CONSTRAINT catch_catch_trapid_fkey FOREIGN KEY (catch_trapid)
      REFERENCES public.trap (trap_id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE RESTRICT,
  CONSTRAINT catch_catch_traptypeid_fkey FOREIGN KEY (catch_traptypeid)
      REFERENCES public.traptype (traptype_id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE RESTRICT,      
  CONSTRAINT catch_catchtypeid_fkey FOREIGN KEY (catch_catchtypeid)
      REFERENCES public.catchtype (catchtype_id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE RESTRICT,
  CONSTRAINT catch_catch_userid_fkey FOREIGN KEY (catch_userid)
      REFERENCES public.users (user_id) MATCH SIMPLE  
      ON UPDATE CASCADE ON DELETE RESTRICT,
  CONSTRAINT catch_baitid_fkey FOREIGN KEY (catch_baitid)
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
  (catch_trapid);


-- Index: public.c_cttypeid_idx
-- DROP INDEX public.c_cttypeid_idx;

CREATE INDEX c_cttypeid_idx
  ON public.catch
  USING btree
  (catch_traptypeid);  


-- Index: public.c_ctypeid_idx
-- DROP INDEX public.c_ctypeid_idx;

CREATE INDEX c_ctypeid_idx
  ON public.catch
  USING btree
  (catch_catchtypeid);


-- Index: public.c_cuid_idx
-- DROP INDEX public.c_cuid_idx;

CREATE INDEX c_cuid_idx
  ON public.catch
  USING btree
  (catch_userid);


-- Index: public.c_cbid_idx
-- DROP INDEX public.c_cbid_idx;

CREATE INDEX c_cbid_idx
  ON public.catch
  USING btree
  (catch_baitid);
