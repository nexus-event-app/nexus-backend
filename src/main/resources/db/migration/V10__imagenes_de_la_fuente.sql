-- Por fuente: usar o no las imágenes que trae (feed y página del artículo). Si no, la web muestra sus propias
-- fotos de portada. DEV genera automáticamente tarjetas de texto como portada, así que se desactiva para ella.
alter table import_sources add column use_source_images boolean default true not null;

update import_sources set use_source_images = false where url = 'https://dev.to/feed/tag/beginners';

update news set image_url = null
where external_id is not null
  and source in (select name from import_sources where use_source_images = false);
