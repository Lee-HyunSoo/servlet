package hello.servlet.web.frontcontroller.v3;

import hello.servlet.web.frontcontroller.ModelView;
import hello.servlet.web.frontcontroller.MyView;
import hello.servlet.web.frontcontroller.v2.controller.MemberFormControllerV2;
import hello.servlet.web.frontcontroller.v3.controller.MemberFormControllerV3;
import hello.servlet.web.frontcontroller.v3.controller.MemberListControllerV3;
import hello.servlet.web.frontcontroller.v3.controller.MemberSaveControllerV3;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Controller의 servlet 종속성 제거
 * 1. controller들이 request객체를 param으로 받던 것 제거
 * 2. frontcontroller에서 request 객체를 분해 -> paramMap을 만들어 controller로 넘겨줌
 * 3. controller는 paramMap을 통해 model을 만들고, model과 view의 논리이름을 modelview 객체에 담아 반환
 * 4. frontcontroller는 돌려받은 modelview 객체의 논리이름을 viewresolver를 통해 물리이름으로 변환
 * 5. render에 modelview 객체를 넘겨 최종 rending
 */
@WebServlet(name = "frontControllerServletV3", urlPatterns = "/front-controller/v3/*")
public class FrontControllerServletV3 extends HttpServlet {

    private Map<String, ControllerV3> controllerMap = new HashMap<>();

    public FrontControllerServletV3() {
        controllerMap.put("/front-controller/v3/members/new-form", new MemberFormControllerV3());
        controllerMap.put("/front-controller/v3/members/save", new MemberSaveControllerV3());
        controllerMap.put("/front-controller/v3/members", new MemberListControllerV3());
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String requestURI = request.getRequestURI();

        // 받아온 URI로 Controller 호출
        ControllerV3 controller = controllerMap.get(requestURI);
        if (requestURI == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // Controller에 넘겨줄 paramMap을 만들기 위해 request로 넘어온 값 다 꺼내줌
        Map<String, String> paramMap = createParamMap(request);
        // Controller의 return 값인 model + view.
        ModelView modelView = controller.process(paramMap);
        // modelview에선 논리이름만 저장되어있기 때문에 해당 논리이름을 물리이름으로 변환
        MyView myView = viewResolver(modelView);
        // render에 모델도 같이 넘겨줌. 이 경우 request는 RequestDispatcher 객체를 호출하기위한 역할만함
        myView.render(modelView.getModel(), request, response);
    }

    private MyView viewResolver(ModelView modelView) {
        return new MyView("/WEB-INF/views/" + modelView.getViewname() + ".jsp");
    }

    private Map<String, String> createParamMap(HttpServletRequest request) {
        Map<String, String> paramMap = new HashMap<>();
        request.getParameterNames().asIterator()
                .forEachRemaining(paramName -> paramMap.put(paramName, request.getParameter(paramName)));
        return paramMap;
    }
}
