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

DROP SCHEMA IF EXISTS public CASCADE;
CREATE SCHEMA public;
ALTER SCHEMA public OWNER TO nestnz;


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
    CONSTRAINT region_pkey PRIMARY KEY (region_id),
    CONSTRAINT region_unique UNIQUE (region_name),

    -- Ensure that region names are short enough to be displayed in the app.
    CONSTRAINT region_name_length CHECK (char_length(region_name) <= 30)
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
    user_isadmin boolean NOT NULL DEFAULT FALSE,
    user_isinactive boolean NOT NULL DEFAULT FALSE,
    CONSTRAINT users_pkey PRIMARY KEY (user_id),

    -- All usernames and emails must be unique
    CONSTRAINT username_unique UNIQUE (user_name),
    CONSTRAINT useremail_unique UNIQUE (user_contactemail),

    -- Phone numbers must be of length 8-15 inclusive, and contain only numerals
    CONSTRAINT valid_phone CHECK (user_contactphone IS NULL OR user_contactphone ~ '^\d{5,14}$'::text),

    -- Usernames must be at least 3 characters long and not contain colons (interferes with basic auth)
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
    session_expirestimestamp timestamp without time zone NOT NULL DEFAULT now()::timestamp + interval '30 minutes',
    CONSTRAINT session_pkey PRIMARY KEY (session_id),
    CONSTRAINT session_token_unique UNIQUE (session_token),
    CONSTRAINT session_session_userid_fkey FOREIGN KEY (session_userid)
        REFERENCES public.users (user_id) MATCH SIMPLE
        ON UPDATE NO ACTION ON DELETE NO ACTION,
    CONSTRAINT valid_session_token 
        CHECK (session_token ~ '^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$'::text)
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
    (session_token COLLATE pg_catalog."default", session_userid);


-- Table: public.bait
-- DROP TABLE public.bait;

CREATE TABLE public.bait
(
    bait_id bigint NOT NULL DEFAULT nextval('bait_bait_id_seq'::regclass),
    bait_name text NOT NULL,
    bait_imagefilename text,
    bait_note text,
    CONSTRAINT bait_pkey PRIMARY KEY (bait_id),

    -- Ensure multiple baits with the same name can't exist.
    CONSTRAINT bait_unique UNIQUE (bait_name),

    -- Ensure that bait names are short enough to be displayed in the app.
    CONSTRAINT bait_name_length CHECK (char_length(bait_name) <= 30)
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

    -- Ensure multiple trap-types with the same name can't exist.
    CONSTRAINT traptype_unique UNIQUE (traptype_name),

    -- Ensure that traptype names are short enough to be displayed in the app.
    CONSTRAINT traptype_name_length CHECK (char_length(traptype_name) <= 30),

    -- Ensure that each trap-type has at least some form of textual identifier.
    CONSTRAINT traptype_valid_text_identifier 
        CHECK (NOT ((traptype_model IS NULL) AND (traptype_name IS NULL)))
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
    trapline_defaulttraptypeid bigint NOT NULL,
    trapline_defaultbaitid bigint NOT NULL,
    CONSTRAINT trapline_pkey PRIMARY KEY (trapline_id),

    -- Ensure that traplines have unique names.
    CONSTRAINT trapline_unique UNIQUE (trapline_name),

    -- Ensure that trapline names are short enough to be displayed in the app.
    CONSTRAINT trapline_name_length CHECK (char_length(trapline_name) <= 30),

    CONSTRAINT trapline_trapline_regionid_fkey FOREIGN KEY (trapline_regionid)
        REFERENCES public.region (region_id) MATCH SIMPLE
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT trapline_trapline_defaulttraptypeid_fkey FOREIGN KEY (trapline_defaulttraptypeid)
        REFERENCES public.traptype (traptype_id) MATCH SIMPLE
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT trapline_trapline_defaultbaitid_fkey FOREIGN KEY (trapline_defaultbaitid)
        REFERENCES public.bait (bait_id) MATCH SIMPLE
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
    CONSTRAINT catchtype_pkey PRIMARY KEY (catchtype_id),

    -- Ensure that catch-types have unique names.
    CONSTRAINT catchtype_unique UNIQUE (catchtype_name),

    -- Ensure that catchtype names are short enough to be displayed in the app.
    CONSTRAINT catchtype_name_length CHECK (char_length(catchtype_name) <= 30)
)
WITH (
    OIDS=FALSE
);
ALTER TABLE public.catchtype
    OWNER TO nestnz;


-- Add the default hard-coded catch-type of 'other' to the table
INSERT INTO public.catchtype
    (catchtype_id, catchtype_name)
VALUES
    (10000, 'Other');



-- Table: public.trapline_user
-- DROP TABLE public.trapline_user;

CREATE TABLE public.traplineuser
(
    traplineuser_id bigint NOT NULL DEFAULT nextval('traplineuser_traplineuser_id_seq'::regclass),
    traplineuser_userid bigint NOT NULL,
    traplineuser_traplineid bigint NOT NULL,
    traplineuser_isadmin boolean NOT NULL DEFAULT FALSE,
    CONSTRAINT traplineuser_pkey PRIMARY KEY (traplineuser_id),

    -- Ensure there is only one mapping between a user and each trapline.
    CONSTRAINT traplineuser_unique UNIQUE (traplineuser_userid, traplineuser_traplineid),

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
    trap_number bigint,
    trap_coordx numeric NOT NULL,
    trap_coordy numeric NOT NULL,
    trap_traptypeid bigint NOT NULL,
    trap_status integer NOT NULL DEFAULT 1,
    trap_createdtimestamp timestamp without time zone NOT NULL DEFAULT now()::timestamp,
    trap_lastresettimestamp timestamp without time zone NOT NULL DEFAULT now()::timestamp,
    trap_baitid bigint NOT NULL,
    CONSTRAINT trap_pkey PRIMARY KEY (trap_id),

    -- Ensure that latitude and longitude values are within valid ranges
    CONSTRAINT valid_latitude CHECK ((trap_coordy >= -90) AND (trap_coordy <= 90)),
    CONSTRAINT valid_longitude CHECK ((trap_coordx >= -180) AND (trap_coordx <= 180)),

    -- Ensure that two traps cannot exist in the same location.
    CONSTRAINT trap_coords_unique UNIQUE (trap_coordx, trap_coordy),

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
    catch_baitid bigint,
    catch_note text,
    catch_loggedtimestamp timestamp without time zone NOT NULL DEFAULT now()::timestamp,
    catch_imagefilename text,
    CONSTRAINT catch_pkey PRIMARY KEY (catch_id),

    -- Ensure that catches sent multiple times from the app cache do not get stored multiple times.
    CONSTRAINT catch_unique UNIQUE (catch_trapid, catch_catchtypeid, catch_loggedtimestamp),

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



-- Trigger functions to enforce non-null values on tables with defaults
-- the request handler uses SQL templates which may return nulls
-- and this doesn't work well with columns set to not accept nulls
-- so we can manually handle these here without too much work,
-- or without remodelling the API.


-- Set default values for columns in users

-- Function: public.user_defaults()
-- DROP FUNCTION public.user_defaults();

CREATE OR REPLACE FUNCTION public.user_defaults()
    RETURNS trigger AS
$BODY$
    BEGIN
        -- Check that all user fields with defaults have values
        IF NEW.user_createdtimestamp IS NULL THEN
            NEW.user_createdtimestamp := now()::timestamp;
        END IF;
        IF NEW.user_isadmin IS NULL THEN
            NEW.user_isadmin := FALSE;
        END IF;
        IF NEW.user_isinactive IS NULL THEN
            NEW.user_isinactive := FALSE;
        END IF;
        RETURN NEW;
    END;
$BODY$
    LANGUAGE plpgsql VOLATILE
    COST 100;
ALTER FUNCTION public.user_defaults()
    OWNER TO nestnz;

-- And link to the events for the column change

-- Trigger: user_defaults_trigger on public.users
-- DROP TRIGGER user_defaults_trigger ON public.users;

CREATE TRIGGER user_defaults_trigger
    BEFORE INSERT OR UPDATE
    ON public.users
    FOR EACH ROW
    EXECUTE PROCEDURE public.user_defaults();


-- Set default values for columns in catch

-- Function: public.catch_defaults()
-- DROP FUNCTION public.catch_defaults();

CREATE OR REPLACE FUNCTION public.catch_defaults()
    RETURNS trigger AS
$BODY$
    BEGIN
        -- Check that all catch fields with defaults have values
        IF NEW.catch_loggedtimestamp IS NULL THEN
            NEW.catch_loggedtimestamp := now()::timestamp;
        END IF;
        IF NEW.catch_baitid IS NULL THEN
            NEW.catch_baitid := (
                SELECT
                    t.trap_baitid
                FROM
                    trap t
                WHERE
                    t.trap_id = NEW.catch_trapid
            );
        END IF;
        IF NEW.catch_traptypeid IS NULL THEN
            NEW.catch_traptypeid := (
                SELECT
                    t.trap_traptypeid
                FROM
                    trap t
                WHERE
                    t.trap_id = NEW.catch_trapid
            );
        END IF;
        RETURN NEW;
    END;
$BODY$
    LANGUAGE plpgsql VOLATILE
    COST 100;
ALTER FUNCTION public.catch_defaults()
    OWNER TO nestnz;



-- And link to the events for the column change

-- Trigger: catch_defaults_trigger on public.catch
-- DROP TRIGGER catch_defaults_trigger ON public.catch;

CREATE TRIGGER catch_defaults_trigger
    BEFORE INSERT OR UPDATE
    ON public.catch
    FOR EACH ROW
    EXECUTE PROCEDURE public.catch_defaults();



-- Set default values for columns in session

-- Function: public.session_defaults()
-- DROP FUNCTION public.session_defaults();

CREATE OR REPLACE FUNCTION public.session_defaults()
    RETURNS trigger AS
$BODY$
    BEGIN
        -- Check that all session fields with defaults have values
        IF NEW.session_createdtimestamp IS NULL THEN
            NEW.session_createdtimestamp := now()::timestamp;
        END IF;
        RETURN NEW;
    END;
$BODY$
    LANGUAGE plpgsql VOLATILE
    COST 100;
ALTER FUNCTION public.session_defaults()
    OWNER TO nestnz;

-- And link to the events for the column change

-- Trigger: session_defaults_trigger on public.session
-- DROP TRIGGER session_defaults_trigger ON public.session;

CREATE TRIGGER session_defaults_trigger
    BEFORE INSERT OR UPDATE
    ON public.session
    FOR EACH ROW
    EXECUTE PROCEDURE public.session_defaults();


-- Set default values for columns in trap

-- Function: public.trap_defaults()
-- DROP FUNCTION public.trap_defaults();

CREATE OR REPLACE FUNCTION public.trap_defaults()
    RETURNS trigger AS
$BODY$
    BEGIN
        -- Check that all trap fields with defaults have values
        IF NEW.trap_createdtimestamp IS NULL THEN
            NEW.trap_createdtimestamp := now()::timestamp;
        END IF;
        IF NEW.trap_lastresettimestamp IS NULL THEN
            NEW.trap_lastresettimestamp := now()::timestamp;
        END IF;
        IF NEW.trap_status IS NULL THEN
            NEW.trap_status := 1;
        END IF;
        IF NEW.trap_baitid IS NULL THEN
            NEW.trap_baitid := (
                SELECT
                    tl.trapline_defaultbaitid
                FROM
                    trapline tl
                WHERE
                    tl.trapline_id = NEW.trap_traplineid
            );
        END IF;
        IF NEW.trap_traptypeid IS NULL THEN
            NEW.trap_traptypeid := (
                SELECT
                    tl.trapline_defaulttraptypeid
                FROM
                    trapline tl
                WHERE
                    tl.trapline_id = NEW.trap_traplineid
            );
        END IF;
        RETURN NEW;
    END;
$BODY$
    LANGUAGE plpgsql VOLATILE
    COST 100;
ALTER FUNCTION public.trap_defaults()
    OWNER TO nestnz;

-- And link to the events for the column change

-- Trigger: trap_defaults_trigger on public.trap
-- DROP TRIGGER trap_defaults_trigger ON public.trap;

CREATE TRIGGER trap_defaults_trigger
    BEFORE INSERT OR UPDATE
    ON public.trap
    FOR EACH ROW
    EXECUTE PROCEDURE public.trap_defaults();


-- Set default values for columns in trap

-- Function: public.traplineuser_defaults()
-- DROP FUNCTION public.traplineuser_defaults();

CREATE OR REPLACE FUNCTION public.traplineuser_defaults()
    RETURNS trigger AS
$BODY$
    BEGIN
        -- Check that all traplineuser fields with defaults have values
        IF NEW.traplineuser_isadmin IS NULL THEN
            NEW.traplineuser_isadmin := FALSE;
        END IF;
        RETURN NEW;
    END;
$BODY$
    LANGUAGE plpgsql VOLATILE
    COST 100;
ALTER FUNCTION public.traplineuser_defaults()
    OWNER TO nestnz;

-- And link to the events for the column change

-- Trigger: traplineuser_defaults_trigger on public.trap
-- DROP TRIGGER traplineuser_defaults_trigger ON public.trap;

CREATE TRIGGER traplineuser_defaults_trigger
    BEFORE INSERT OR UPDATE
    ON public.traplineuser
    FOR EACH ROW
    EXECUTE PROCEDURE public.traplineuser_defaults();


-- Functions called to handle session 


-- Delete all expired sessions whenever called

-- Function: public.clean_sessions()
-- DROP FUNCTION public.clean_sessions();

CREATE OR REPLACE FUNCTION public.clean_sessions()
    RETURNS void AS
$BODY$
    BEGIN
        DELETE FROM public.session
        WHERE session_expirestimestamp < now()::timestamp;
    END;
$BODY$
    LANGUAGE plpgsql VOLATILE
    COST 100;
ALTER FUNCTION public.clean_sessions()
    OWNER TO nestnz;


-- Clean all old sessions, then extend the specified session if it exists.

-- Function: public.clean_sessions()
-- DROP FUNCTION public.clean_sessions();

CREATE OR REPLACE FUNCTION public.extend_session(token text, length interval)
    RETURNS timestamp AS
$BODY$
    DECLARE
    new_expires timestamp;
    BEGIN
        PERFORM public.clean_sessions();

        UPDATE session
        SET session_expirestimestamp = GREATEST(now()::timestamp + length, session_expirestimestamp)
        WHERE session_token = token
        RETURNING session_expirestimestamp

        INTO new_expires;

        RETURN new_expires;
    END;
$BODY$
    LANGUAGE plpgsql VOLATILE
    COST 100;
ALTER FUNCTION public.extend_session(token text, length interval)
    OWNER TO nestnz;
