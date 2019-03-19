package com.pgy.ups.pay.service.dao;



import com.pgy.ups.pay.interfaces.entity.UpsOrderEntity;
import com.pgy.ups.pay.interfaces.form.UpsOrderForm;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


@Repository
public interface UpsOrderDubboDao extends JpaRepository<UpsOrderEntity, Long> {

    @Query(value = " SELECT  * FROM ups_t_order c  WHERE c.from_system = :#{#form.fromSystem} AND IF (:#{#form.orderType} !='',c.order_type = :#{#form.orderType}  ,1=1) AND IF (:#{#form.bankMd5} !='',c.bank_md5 = :#{#form.bankMd5} ,1=1)  AND IF (:#{#form.upsOrderCode} !='',c.ups_order_code = :#{#form.upsOrderCode} ,1=1)  AND IF (:#{#form.businessFlowNum} !='',c.business_flow_num = :#{#form.businessFlowNum} ,1=1) AND IF (:#{#form.id} !='',c.id = :#{#form.id} ,1=1) AND IF (:#{#form.userNo} !='',c.user_no = :#{#form.userNo} ,1=1) AND IF (:#{#form.orderStatus} !='',c.order_status = :#{#form.orderStatus} ,1=1) AND IF (:#{#form.payChannel} !='',c.pay_channel = :#{#form.payChannel} ,1=1) ORDER BY c.create_time desc ",nativeQuery = true)
    Page<UpsOrderEntity> getPage(@Param("form") UpsOrderForm form, Pageable pageable);

}