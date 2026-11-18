-- «Me apunto»: cuántas personas piensan ir a cada evento (solo se muestra el número, nunca quién).
create table event_interests (
    event_id bigint not null,
    user_id bigint not null,
    created_at timestamp(6) not null,
    constraint pk_event_interests primary key (event_id, user_id),
    constraint fk_event_interests_event foreign key (event_id) references community_events (id) on delete cascade,
    constraint fk_event_interests_user foreign key (user_id) references users (id) on delete cascade
);
