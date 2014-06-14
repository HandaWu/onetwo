USE [ICCARD]
GO
/****** Object:  StoredProcedure [dbo].[SEND_CLEAR_ACARD_REGION]    Script Date: 01/15/2014 11:11:04 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO

ALTER PROCEDURE [dbo].[SEND_CLEAR_ACARD_REGION]
(	
) AS
BEGIN TRY

--根据a卡数据创建临时地区表
drop table send_acard_region;
select * 
into send_acard_region
from (
		select 
			sa.countyname, sa.countycode, sa.townname, sa.towncode, sa.villagename, sa.villagecode
		from send_acard sa
			where sa.cardtype=3
		group by
		sa.countyname, sa.countycode, sa.townname, sa.towncode, sa.villagename, sa.villagecode
) re

----更新县区数据，把不同的名称和代码补充到正式表的all_name字段
select *  
update ar set ar.all_name=('/'+ar.name+'/'+dist.name+'/'), ar.all_ccode=('/'+ar.ccode+'/'+dist.ccode+'/')
from admin_region_bak ar
left join (
	--check_start检查不在正式地区表的县区数据
	select * from (	
		select 
			sar.countyname as name, 
			sar.countycode as ccode,
			'441800000' AS parent_code,
			'DISTRICT' as clevel
		from send_acard_region sar
		group by  sar.countyname, sar.countycode
	) city where city.name  not in(
		select name from admin_region_bak ar where ar.clevel='DISTRICT'
	)
	--check_end检查不在正式地区表的县区数据
	and city.name='清新县'
) dist on SUBSTRING(dist.name, 1, 2)=SUBSTRING(ar.name, 1, 2)
where dist.name is not null


END TRY
BEGIN CATCH 
	ROLLBACK TRANSACTION;
	PRINT ERROR_MESSAGE();
	PRINT ERROR_STATE();
	PRINT ERROR_LINE();
	RETURN -1;  
END CATCH
