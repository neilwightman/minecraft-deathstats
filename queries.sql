sqlite3 ~/deathstats/sqlite.db

select * from death_log where dt > (select start from session where name = 'session' order by start desc limit 1);

select count(*) from death_log where dt > (select start from session where name = 'session' order by start desc limit 1);

INSERT INTO DEATH_LOG(world,dimension,message,killedByKey,killedByStr,argb) VALUES('New World','overworld','death.attack.explosion.player','entity.minecraft.creeper',NULL,16777215);

.schema session
.schema death_log

-- Deaths weekday
SELECT WeekDay,COUNT(*) AS WeekDayCount FROM
(SELECT *,strftime('%w',dt) AS WeekDay
FROM death_log) T1
GROUP BY T1.WeekDay

-- deaths per hour
select hour,count(*) as hour from (SELECT *,strftime('%H',dt) as hour from death_log) group by hour;

-- deaths per day today.
select hour,count(*) as hour from (SELECT *,strftime('%H',dt) as hour from death_log where death_log.dt > date('now','start of day','localtime')) group by hour;

-- deaths per day last 24 hours?

--deaths per month
select month, count(*) as month from (select *, strftime('%m',dt) as month from death_log) T1 group by T1.month;

--death per day of the month
select month, count(*) as month from (select *, strftime('%d',dt) as month from death_log) T1 group by T1.month;

--Use local time
select datetime(dt, 'localtime') from death_log;

--Deaths per hour, local time.
--Start end need to be local time where clause too.
select month, count(*) as month from (select *, strftime('%H',datetime(dt,'localtime')) as month from death_log) T1 group by T1.month;

--List deaths with session info
select session.id,session.start,session.end,death_log.dt from 'session' join 'death_log' on death_log.dt > session.start and death_log.dt < session.end;

--count deaths per session
select session.id,count(session.id) from 'session' join 'death_log' on death_log.dt > session.start and death_log.dt < session.end group by session.id;
select session.id,session.start,count(session.id) from 'session' join 'death_log' on death_log.dt > session.start and death_log.dt < session.end group by session.id;

--- max deaths per session
select session.id,session.start,count(session.id) as deaths from 'session' join 'death_log' on death_log.dt > session.start and death_log.dt < session.end group by session.id order by deaths DESC limit 1;

-- deaths by killer
select count(*),message, killedbykey from death_log group by message, killedbykey, killedbystr;
-- could limit to top n

select count(*) as cnt, message, killedbykey, killedbystr, min(argb) from death_log group by message, killedbykey, killedbystr order by cnt desc;

.headers ON