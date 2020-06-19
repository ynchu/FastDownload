insert into `user`
values ('123456', '123456', 1),
       ('123457', '123456', 2);

select *
from user
where id = '123456';

update user
set pwd  = '',
    type = 1
where id = '';

delete
from user
where id = '';




select *
from file_info;

select *
from file_info
where md5 = '';



insert into `fastdownload`.`file_info`
values ('12534', '1.flv', 'C:\\FastDownload\\Data\\1.flv', 1, null);

delete
from file_info
where md5 = '12534';

update file_info
set name     = '',
    location = '',
    count    = 1,
    url      = ''
where md5 = '12534';
