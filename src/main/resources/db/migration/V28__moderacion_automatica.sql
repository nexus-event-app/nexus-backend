alter table reports alter column reporter_id drop not null;
alter table discussions add column held boolean not null default false;
alter table comments add column held boolean not null default false;
alter table testimonials add column held boolean not null default false;
alter table community_events add column held boolean not null default false;
