


# SQL面试题  



查询两门以上不及格课程的同学的学号，以及不及格课程的平均成绩

select 学号, avg(case when 成绩<60 then 成绩 else null end)

from score

group by 学号

having sum(case when 成绩<60 then 1 else 0 end)>=2;

按原数据就是查不出结果，懒得改数据了，不知道为什么我特别喜欢用case when


