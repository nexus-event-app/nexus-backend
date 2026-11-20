-- La descripción de un evento admite hasta 2000 caracteres: con 280, el texto pegado de Meetup o Eventbrite se
-- cortaba a mitad de frase.
alter table community_events alter column description type varchar(2000);
