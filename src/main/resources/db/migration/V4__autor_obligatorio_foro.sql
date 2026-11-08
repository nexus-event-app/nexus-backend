-- Toda discusión y todo comentario tiene autor registrado: se elimina el "autor anónimo".
-- El contenido sin autor es anterior al registro obligatorio, no se puede atribuir a nadie y se descarta.
delete from comment_likes
 where comment_id in (select id from comments
                       where user_id is null
                          or discussion_id in (select id from discussions where user_id is null));
delete from comments
 where user_id is null
    or discussion_id in (select id from discussions where user_id is null);
delete from discussion_likes
 where discussion_id in (select id from discussions where user_id is null);
delete from discussions where user_id is null;

alter table discussions alter column user_id set not null;
alter table comments alter column user_id set not null;
