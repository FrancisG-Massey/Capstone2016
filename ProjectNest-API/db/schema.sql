/*********************************************************
 *	Copyright (C) Project Nest NZ (2016)
 *	Unauthorized copying of this file, via any medium is 
 *		strictly prohibited.
 *	Proprietary and confidential
 *	Written by Sam Hunt <s2112201@ipc.ac.nz>, August 2016
 *********************************************************/

-- Nest Database Initial Setup SQL Script

CREATE TABLE region (
	region_id bigserial NOT NULL,
	region_name text NOT NULL,
	region_doc_id text NOT NULL,

	PRIMARY KEY (region_id)
);

CREATE TABLE users (
	user_id bigserial NOT NULL,
	user_name text NOT NULL,
	user_password text NOT NULL,
	user_contact_phone text DEFAULT NULL,
	user_contact_email text DEFAULT NULL,
	user_created_timestamp timestamp NOT NULL DEFAULT now(),
	user_created_user_id bigint DEFAULT NULL,
	user_is_admin boolean NOT NULL DEFAULT FALSE,
	user_is_inactive boolean NOT NULL DEFAULT FALSE,

	PRIMARY KEY (user_id),
	
	-- Ensure phone numbers are between 5-14 in length and contain only numerals.
	CONSTRAINT valid_phone
		CHECK ((user_contact_phone IS NULL) OR (user_contact_phone ~ '^\d{5,14}$'))
);

CREATE TABLE bait (
	bait_id bigserial NOT NULL,
	bait_name text NOT NULL,
	bait_image_filename text DEFAULT NULL,
	bait_note text DEFAULT NULL,

	PRIMARY KEY (bait_id)
);

CREATE TABLE trap_type (
	trap_type_id bigserial NOT NULL,
	trap_type_name text DEFAULT NULL,
	trap_type_model text DEFAULT NULL,
	trap_type_note text DEFAULT NULL,

	PRIMARY KEY (trap_type_id),

	-- Ensure that at least one type of textual identifier is supplied.
	CONSTRAINT trap_type_valid_text_identifier
		CHECK (NOT (trap_type_model IS NULL) AND (trap_type_name IS NULL))
);

CREATE TABLE trap_line (
	trap_line_id bigserial NOT NULL,
	trap_line_name text NOT NULL,
	trap_line_doc_id text NOT NULL,
	trap_line_region_id bigint NOT NULL,
	trap_line_start_tag bigint DEFAULT NULL,
	trap_line_end_tag bigint DEFAULT NULL,
	trap_line_image_filename text DEFAULT NULL,

	PRIMARY KEY (trap_line_id),
	FOREIGN KEY (trap_line_region_id) REFERENCES region(region_id) 
		ON UPDATE CASCADE ON DELETE RESTRICT
);
CREATE INDEX tl_rid_idx ON trap_line (trap_line_region_id);

CREATE TABLE role (
	role_id bigserial NOT NULL,
	role_name text NOT NULL,

	PRIMARY KEY (role_id)
);

CREATE TABLE catch_type (
	catch_type_id bigserial NOT NULL,
	catch_type_name text NOT NULL,
	catch_type_image_filename text DEFAULT NULL,

	PRIMARY KEY (catch_type_id)
	
);

CREATE TABLE trap_line_user (
	trap_line_user_id bigserial NOT NULL,
	trap_line_user_user_id bigint NOT NULL,
	trap_line_user_trap_line_id bigint NOT NULL,
	trap_line_user_role_id bigint NOT NULL,

	PRIMARY KEY (trap_line_user_id),
	FOREIGN KEY (trap_line_user_user_id) REFERENCES users(user_id) 
		ON UPDATE CASCADE ON DELETE RESTRICT,
	FOREIGN KEY (trap_line_user_trap_line_id) REFERENCES trap_line(trap_line_id)
		ON UPDATE CASCADE ON DELETE RESTRICT,
	FOREIGN KEY (trap_line_user_role_id) REFERENCES role(role_id)
		ON UPDATE CASCADE ON DELETE RESTRICT
);
CREATE INDEX tlu_uid_idx ON trap_line_user (trap_line_user_user_id);
CREATE INDEX tlu_tlid_idx ON trap_line_user (trap_line_user_trap_line_id);
CREATE INDEX tlu_rid_idx ON trap_line_user (trap_line_user_role_id);



CREATE TABLE trap (
	trap_id bigserial NOT NULL,
	trap_line_id bigint NOT NULL,
	trap_doc_id text NOT NULL,
	trap_coord_x decimal NOT NULL,
	trap_coord_y decimal NOT NULL,
	trap_type_id bigint NOT NULL,
	trap_status int NOT NULL DEFAULT 1,
	trap_created timestamp NOT NULL DEFAULT now(),
	trap_last_reset timestamp NOT NULL DEFAULT now(),
	trap_bait_id bigint DEFAULT NULL,

	PRIMARY KEY (trap_id),
	FOREIGN KEY (trap_line_id) REFERENCES trap_line(trap_line_id)
		ON UPDATE CASCADE ON DELETE RESTRICT,
	FOREIGN KEY (trap_type_id) REFERENCES trap_type(trap_type_id)
		ON UPDATE CASCADE ON DELETE RESTRICT,
	FOREIGN KEY (trap_bait_id) REFERENCES bait(bait_id)
		ON UPDATE CASCADE ON DELETE RESTRICT
);

CREATE INDEX t_tlid_idx ON trap (trap_line_id); 
CREATE INDEX t_ttid_idx ON trap (trap_type_id);
CREATE INDEX t_btid_idx ON trap (trap_bait_id);

CREATE TABLE catch (
	catch_id bigserial NOT NULL,
	catch_user_id bigint NOT NULL,
	catch_trap_id bigint NOT NULL,
	catch_type_id bigint NOT NULL,
	catch_note text DEFAULT NULL,
	catch_logged timestamp NOT NULL DEFAULT now(),
	catch_image_filename text DEFAULT NULL,

	PRIMARY KEY (catch_id),
	FOREIGN KEY (catch_user_id) REFERENCES users(user_id)
		ON UPDATE CASCADE ON DELETE RESTRICT,
	FOREIGN KEY (catch_trap_id) REFERENCES trap(trap_id)
		ON UPDATE CASCADE ON DELETE RESTRICT,
	FOREIGN KEY (catch_type_id) REFERENCES catch_type(catch_type_id)
		ON UPDATE CASCADE ON DELETE RESTRICT
);

CREATE INDEX c_cuid_idx ON catch (catch_user_id); 
CREATE INDEX c_ctrapid_idx ON catch (catch_trap_id); 
CREATE INDEX c_ctypeid_idx ON catch (catch_type_id); 
--CREATE INDEX c_dates_idx ON catch(catch_logged::date);