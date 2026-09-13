ALTER TABLE mep ADD COLUMN ep_member_id BIGINT;
ALTER TABLE mep ADD CONSTRAINT uk_mep_ep_member_id UNIQUE (ep_member_id);