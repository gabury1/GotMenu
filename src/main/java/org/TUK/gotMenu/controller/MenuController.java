package org.TUK.gotMenu.controller;

import lombok.RequiredArgsConstructor;
import org.TUK.gotMenu.entity.Menu;
import org.TUK.gotMenu.form.MenuForm;
import org.TUK.gotMenu.service.MenuService;
import org.TUK.gotMenu.service.SecurityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;

@RequestMapping("/menu")
@Controller
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;
    @Autowired
    private final SecurityService securityService;

    @RequestMapping("/list")
    public String list(Model model, @RequestParam(value="page", defaultValue="0") int page, @RequestParam(value = "kw", defaultValue = "") String kw){
        Page<Menu> paging = this.menuService.getList(page, kw);
        model.addAttribute("paging", paging);	//템플릿에 페이징에 대한 내용을 넘겨줌
        model.addAttribute("kw", kw);   //템플릿에 검색 키워드에 대한 내용을 넘겨줌
        return "/menu/menuList";
    }

    @RequestMapping("/{menuNo}/detail")
    public String detail(Model model, @PathVariable("menuNo") Integer id) {
        Menu menu = this.menuService.getMenu(id);
        model.addAttribute("menu", menu);	//템플릿에 질문의 상세 내용을 넘겨줌
        return "/menu/menuDetail";
    }

    @GetMapping("/create")
    public String create(MenuForm menuForm){

        if(securityService.getUserNo() == null) return "redirect:/menu/list";
       return "/menu/menuForm";
    }

    @PostMapping("/create")
    public String create(@Valid MenuForm menuForm, BindingResult bindingResult){
        if (bindingResult.hasErrors()) {
            return "menuForm";
        }
        this.menuService.create(menuForm.getMenuComposition(), menuForm.getMenuDetail(), menuForm.getMenuDescription(), 0, menuForm.getTags());
        return "redirect:/menu/list";
    }

    @GetMapping("/{menuNo}/modify")
    public String modify(MenuForm menuForm, @PathVariable("menuNo") Integer menuNo){
        Menu menu = this.menuService.getMenu(menuNo);
        if(!securityService.isSameUser(menu.getWriter().getUserNo()))
            return "redirect:/menu/list";

        menuForm.setMenuDescription(menu.getMenuDescription());
        menuForm.setMenuDetail(menu.getMenuDetail());
        menuForm.setMenuComposition(menu.getMenuComposition());
        menuForm.setTags(menu.getTags());
        return "/menu/menuForm";
    }


    @PostMapping("/{menuNo}/modify")
    public String modify(@Valid MenuForm menuForm, @PathVariable("menuNo") Integer menuNo, BindingResult bindingResult){
        Menu menu = this.menuService.getMenu(menuNo);
        if(bindingResult.hasErrors()){
            return "menuForm";
        }
        this.menuService.modify(menu, menuForm.getMenuComposition(), menuForm.getMenuDetail(), menuForm.getMenuDescription(), menuForm.getTags());
        return String.format("redirect:/menu/%s/detail",menuNo);
    }


    @GetMapping("/{menuNo}/delete")
    public String delete(@PathVariable("menuNo") Integer menuNo){
        Menu menu = this.menuService.getMenu(menuNo);
        if(!securityService.isSameUser(menu.getWriter().getUserNo()))
            return "redirect:/menu/list";

        this.menuService.delete(menu);
        return "redirect:/menu/list";
    }

    //메뉴 리스트 페이지의 서브프레임 제공
    @RequestMapping("/list/subframe")
    public String filter(){
        return "/menu/menuList_subframe";
    }

    @GetMapping("/")
    public void getMenuList(HttpServletResponse response,
                            @Param("target") String target, @Param("keyword") String keyword, @Param("pageNo") int pageNo)
    {
        try
        {
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/json");
            response.getWriter().println(menuService.getMenuList(target, keyword, pageNo));

        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @GetMapping("/{menuNo}")
    public void getMenuInfo(HttpServletResponse response, @PathVariable("menuNo") int menuNo)
    {
        try
        {
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/json");
            response.getWriter().println(menuService.getMenuInfo(menuNo));

        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @RequestMapping("/test")
    @ResponseBody
    private String test()
    {
        String[] comp = new String[]{
              "미역국, 밥",
              "햄버거, 된장찌개, 양파튀김, 던킨 도넛츠 아메리카노",
              "신라면 블랙, 평양 냉면, 무우순볶음밥",
              "강된장비빔밥, 그래놀라 시리얼, O2 음료",
              "임연수어 구이, 레몬사워 크림, 능금 사과주스",
              "롯데리아 모짜렐라인더버거, 스타벅스 아메리카노, 종갓집 김치"
        };
        String[] detail = new String[]{
                "300/500/233/123",
                "632/213/332/394",
                "872/346/853/344",
                "532/843/344/231",
                "452/742/57/321",
                "364/175/479/586",
        };
        String[] des = new String[]{
                "재료는 마트가서 사오세요. ? 싫으면 먹지 마세요.",
                "히히 맛있겠죠? 된샷추라는겁니다.",
                "근데 저는 오늘 황태국에 밥 말아 먹었어요ㅋㅋ",
                "김수한무 거북이와 두루미 삼천갑자 동방삭 치치카포 사리사리센타 워리워리 세브리깡 무두셀라 구름이 허리케인에 담벼락 담벼락에 서생원 서생원에 고양이 고양이엔 바둑이 바둑이는 돌돌이",
                "이 메뉴에 대해 설명해볼게요! 음... 근데 저도 먹어본적이 없어서..ㅎㅎ ㅈㅅ!",
                "우엉이 어떻게 울게요? 우~엉ㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋ",
        };
        String[] tags = new String[]{
                "#마싯음주의#개마싱ㅆ음#함무바라!",
                "#건강식#맛있음#뜨뜻함#백종원레시피#뿌주부#디너파티",
                "#다이어트#여름엔비키니#저염식#건강#치팅데이",
                "#칼로리폭탄#침이줄줄#허기#공복#운동#해야되는데#오늘만",
                "#마왕레시피#매콤달콤#다이어트불가#햄볶#고염식",
                "#영감이#떠오르는맛#괴식#친구#놀리기#의외로#맛있을지도?#그럴리가"
        };

        if(securityService.getUserNo() == null) return "로그인 후 이용 바람!";

        for (int i = 0 ; i < 1000 ; i ++){
            this.menuService.create(comp[i%6], detail[(i*(i+1))%6], des[((i+6)*(i+2))%6], i, tags[(i*i)%6]);
        }
        return Integer.toString(securityService.getUserNo());
    }

}
