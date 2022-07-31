package org.TUK.gotMenu.service;

import lombok.RequiredArgsConstructor;
import org.TUK.gotMenu.DataNotFoundException;
import org.TUK.gotMenu.entity.Menu;
import org.TUK.gotMenu.entity.User;
import org.TUK.gotMenu.repository.MenuRepository;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.json.JSONObject;


@Service
@RequiredArgsConstructor
public class MenuService {

    private final MenuRepository menuRepository;
    @Autowired
    private final CommentService commentService;
    @Autowired
    private final SecurityService securityService;


    public void create(String menuComposition, String menuDetail, String menuDescription, int menuRating, String tags) {
        Menu m = new Menu();
        m.setMenuComposition(menuComposition);
        m.setMenuDetail(menuDetail);
        m.setMenuDescription(menuDescription);
        m.setMenuRating(menuRating);
        m.setTags(tags);

        m.setViews(0);
        m.setRaters("");

        // 작성자에 대한 정보를 등록
        User writer = new User();
        writer.setUserNo(securityService.getUserNo());
        m.setWriter(writer);

        this.menuRepository.save(m);
    }

    public Page<Menu> getList(int page, String kw) {    //MenuList 템플릿에 넘겨 페이징을 위해 사용
        List<Sort.Order> sorts = new ArrayList<>();
        sorts.add(Sort.Order.desc("menuNo"));
        Pageable pageable = PageRequest.of(page, 30, Sort.by(sorts));
        return this.menuRepository.findAllByKeyword(kw, pageable);
    }

    public Menu getMenu(Integer menuNo) {
        Optional<Menu> menu = this.menuRepository.findById(menuNo);
        if (menu.isPresent()) {
            return menu.get();
        } else {
            throw new DataNotFoundException("question not found");
        }
    }

    public void modify(Menu menu, String menuComposition, String mennuDetail, String menuDescription, String tags) {
        menu.setMenuComposition(menuComposition);
        menu.setMenuDetail(mennuDetail);
        menu.setMenuDescription(menuDescription);
        menu.setTags(tags);
        this.menuRepository.save(menu);
    }

    public void delete(Menu menu) {
        // 내부에 있는 코멘트들을 직접 삭제해줘야 User와의 연관이 해결되어 삭제할 수 있다.
        menu.getCommentList().stream().forEach(comment -> commentService.delete(comment.getCommentNo()));
        this.menuRepository.delete(menu);
    }

    public String[] splitTags(Menu menu) {
        String joinedTags = menu.getTags();
        String[] splitedTags = joinedTags.split("#");
        return splitedTags;
    }


    public List<Menu> filter(String flt) {
        List<Menu> menus = this.menuRepository.findAll();
        List<Menu> tmp = new ArrayList<Menu>();
        for (Menu m : menus) {
            for (String s : splitTags(m)) {
                if (s == flt) {
                    tmp.add(m);
                    break;
                }
            }
        }
        return tmp;
    }

    public String getMenuList(String target, String keyword, int pageNo)
    {
        JSONObject object = new JSONObject();

        Page<Menu> page;
        Pageable pageable = PageRequest.of(pageNo, 20, Sort.by("menu_no").descending());
        if(target.equals("메뉴")) page = menuRepository.findByMenuComposition(keyword, pageable);
        else if(target.equals("메뉴 설명")) page = menuRepository.findByMenuDetail(keyword, pageable);
        else if(target.equals("태그")) page = menuRepository.findByTags(keyword, pageable);
        else if(target.equals("글쓴이"))
        {
            // 얘는 JPQL이라 필드명으로 넣어줘야한다..
            pageable = PageRequest.of(pageNo, 10, Sort.by("menuNo").descending());
            page = menuRepository.findByWriterId(keyword, pageable);
        }
        else page = menuRepository.findByMenuComposition("", pageable);

        // 페이징 버튼 정보
        int startBtn = pageNo - 2;
        int endBtn = pageNo + 3;
        int nowBtn = pageNo;

        // 버튼 넣어주기
        if(startBtn < 0) {
            startBtn = 0;
            if(5 <= page.getTotalPages())endBtn = 5;
            else endBtn = page.getTotalPages();
        }
        else if(page.getTotalPages() < endBtn) {
            startBtn = page.getTotalPages()-5;
            endBtn = page.getTotalPages();
        }

        // 글 정보 담기
        JSONArray array = new JSONArray();
        for(Menu m : page)
        {
            JSONObject temp = new JSONObject();
            temp.put("menuNo", m.getMenuNo());
            temp.put("menuComposition", m.getMenuComposition());
            temp.put("menuRating", m.getMenuRating());
            temp.put("tags", m.getTags());
            temp.put("views", m.getViews());

            array.put(temp);
        }

        // 담아주기
        object.put("startBtn", startBtn);
        object.put("endBtn", endBtn);
        object.put("nowBtn", nowBtn);
        object.put("array", array);

        return object.toString();
    }

    public JSONObject getMenuInfo(int menuNo)
    {
        JSONObject object = new JSONObject();
        Menu menu = menuRepository.findByMenuNo(menuNo);

        if(menu == null) return object;

        // 데이터를 JSON에 담아 프론트에 전송송
        object.put("menuNo", menu.getMenuNo());
        object.put("composition", menu.getMenuComposition());
        // 제대로 안나눠진 경우
        try{
            String[] nutritions = menu.getMenuDetail().split("\\/");  // \\<= 이거 안넣어주면 구분자까지 반환한다.
            object.put("calorie", nutritions[0]); // 칼로리
            object.put("carbohydrate", nutritions[1]); // 탄수화물
            object.put("protein", nutritions[2]); // 단백질
            object.put("fat", nutritions[3]); // 지방
        }
        catch (RuntimeException e)
        {
            object.put("calorie", 0); // 칼로리
            object.put("carbohydrate", 0); // 탄수화물
            object.put("protein", 0); // 단백질
            object.put("fat", 0); // 지방
        }

        object.put("description", menu.getMenuDescription());
        object.put("tags", menu.getTags());
        object.put("views", menu.getViews());
        object.put("rating", menu.getMenuRating());
        object.put("writerNo", menu.getWriter().getUserNo());
        object.put("writerId", menu.getWriter().getId());

        return object;
    }


    //메뉴 랜덤으로 골라서 json으로 반환
    public String randomMenu(){
        List<Menu> all = this.menuRepository.findAll();

        Menu menu = all.get((int)(Math.random()*all.size()));//

        JSONObject cJson = new JSONObject();
        cJson.put("menuMenuComposition", menu.getMenuComposition());
        cJson.put("menuTags", menu.getTags());

        return cJson.toString();
    }



}
