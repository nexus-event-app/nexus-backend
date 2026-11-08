-- Las ofertas ubicadas en España se listan primero. El valor lo calcula la aplicación a partir de la ubicación.
alter table job_offers add column in_spain boolean default false not null;
update job_offers set in_spain = true
 where lower(location) like '%españa%' or lower(location) like '%spain%'
    or lower(location) like '%madrid%' or lower(location) like '%barcelona%';
create index idx_jobs_in_spain on job_offers (in_spain);
