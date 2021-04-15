package person.pluto;

import lombok.extern.slf4j.Slf4j;
import person.pluto.natcross2.executor.NatcrossExecutor;
import person.pluto.natcross2.nio.NioHallows;
import person.pluto.natcrossclient.NatcrossClientControl;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Component;

/**
 * <p>
 * spring boot 结束时要执行的任务
 * </p>
 *
 * @author wangmin1994@qq.com
 * @since 2019-03-28 11:01:14
 */
@Slf4j
@Component
public class DestroyMainDisposable implements DisposableBean {

    @Override
    public void destroy() {
        log.debug("DestroyMainDisposable destroy");
        NatcrossClientControl.closeAll();
        NatcrossExecutor.shutdown();
        NioHallows.INSTANCE.cancel();
    }

}
