package cn.edu.hebtu.software.listendemo.credit.interf;


import cn.edu.hebtu.software.listendemo.credit.model.CalendarDate;

/**
 * Created by ldf on 17/6/2.
 */

public interface OnSelectDateListener {
    void onSelectDate(CalendarDate date);

    void onSelectOtherMonth(int offset);//点击其它月份日期
}
