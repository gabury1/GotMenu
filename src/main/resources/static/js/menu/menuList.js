
// 초기값
let target = "메뉴";
let keyword = "";
let pageNo = 0;

// 검색 함수
function search()
{

    target = $("#target").val();
    keyword = $("#search_kw").val();
    pageNo = 0;

    $.ajax({
        url : "/menu/",
        data : {"target" : target, "keyword" : keyword, "pageNo" : pageNo},
        method : "GET",
        success : createList,
        error :
            e => alert(e.responseText)
    })

}
// 페이지를 바꿔주는 함수
function pageChange(number)
{
    pageNo = number;
    $.ajax({
        url : "/menu/",
        data : {"target" : target, "keyword" : keyword, "pageNo" : pageNo},
        method : "GET",
        success : createList,
        error :
            e => alert(e.responseText)
    })
}

// 페이지를 받아오는데 성공했다면 그걸 삽입해주는 함수
function createList(object)
{
    resetSummary();
    if(object.array.length == 0)
    {
        $("#menuBox").html('<div class="menuList-row"><label style="text-decoration: italic; margin: 50px;">no result</label></div>');
        $("#pageBox").html("");
        return;
    }

    var menuHtml = "";
    for(let i=0; i < object.array.length; i++)
    {
        menuHtml +=
        '<div class="menuList-row" onclick="getSummary(' + object.array[i].menuNo + ',' + i + ')">'+
        '	<a href="#" class="menu-title">' + object.array[i].menuComposition +'</a>'+
        '	<span class="menu-tags">'+ object.array[i].tags +'</span>'+
        '	<span class="menu-viewed">' +  object.array[i].views + '</span>'+
        '	<span class="menu-likes">' + object.array[i].menuRating + '</span>'+
        '</div>';
    }

    var pageHtml = ""
    for(let i = object.startBtn; i < object.endBtn; i++)
    {
        if(object.nowBtn == i)
        pageHtml +=
        '<button class="pageBtn" onclick="pageChange(' + i + ')" style="text-decoration: underline; font-weight: bold;">' + (i+1) + '</button>';
        else
        pageHtml +=
        '<button class="pageBtn" onclick="pageChange(' + i + ')">' + (i+1) + '</button>';
    }

    $("#menuBox").html(menuHtml);
    $("#pageBox").html(pageHtml);
}

let mode = false;
let nowNo = null;
let nowIndex = null;

function getSummary(number, index)
{
    if(nowNo == number) mode = false;
    else mode = true;

    if(mode == true)
    {
        nowNo = number;
        $(".menuList-row:eq(" + nowIndex + ")").removeClass('clicked');

        $.ajax({
            url : "/menu/" + number,
            method : "GET",
            success : insertSummary,
            error : e => alert(e.responseText)
        });

        nowIndex = index;
        $(".menuList-row:eq(" + index + ")").addClass('clicked');
        $(".mainframe").addClass('smaller');
        $("#subframe").show();
    }
    else
    {
        $(".menuList-row:eq(" + index + ")").removeClass('clicked');
        resetSummary();
    }

}
function resetSummary()
{

    $(".mainframe").removeClass('smaller');
    $("#subframe").hide();
    nowNo = null;
    nowIndex = null;
}

function insertSummary(object)
{
    var subframe = $("#subframe").contents();
    subframe.find("#title").text(object.composition);
    subframe.find("#writer").text(object.writerId);
    subframe.find("#tags").text(object.tags);
    subframe.find("#calorie").text(object.calorie + "kcal");
    subframe.find("#rating").text(object.rating);
}

