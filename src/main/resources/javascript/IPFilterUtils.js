function switchErrorRows()
{
    var parentId = "";
    $(".fieldError").each(function()
    {
        if($(this).html().length>0)
        {
            parentId = $(this).attr("parentId");
        }
    });
    switchRow(parentId);
}


function switchRow(elementId)
{
    //building css element id
    elementId="#"+elementId;

    //building css form id
    var elementFormId = elementId+"_form";

    //Checking which element to show and which element to hide
    if( $(elementId).is(":visible"))
    {
        if(currentForm!='')
        {
            $(currentForm).hide();
            $(currentForm+" .updateField").attr("disabled","disabled");
            $(currentForm+" .statusSwitch").bootstrapSwitch();
            $(currentElement).show();
        }
        //Hide the display row
        $(elementId).hide();
        //Show the form
        $(elementFormId).show();
        $(elementFormId+" .updateField").prop("disabled",false);
        $(elementFormId+" .statusSwitch").bootstrapSwitch();

    }
    else
    {
        //Hide the Form
        $(elementFormId).hide();
        $(elementFormId+" .updateField").attr("disabled","disabled");
        $(elementFormId+" .statusSwitch").bootstrapSwitch();
        //Show the display Row
        $(elementId).show();
    }
    currentElement = elementId;
    currentForm = elementFormId;
    $(currentForm+" .bootstrap-switch").removeClass("bootstrap-switch-disabled");
}


function ApplyRuleConstraints()
{
    var currentSite = $(".sites select").val();
    if (currentSite in philosophiesMap)
    {
        var currentConstraint = philosophiesMap[currentSite];
        if(currentConstraint!=undefined && currentConstraint != "")
        {
            $(".ruleType input:text").show();
            $(".ruleType input:text").prop('disabled', false);
            $(".ruleType input:text").val(philosophiesDisplayMap[currentSite]);
            $(".ruleType input:hidden").val(philosophiesMap[currentSite]);
            $(".ruleType input:text").attr('readOnly', 'readOnly');
            $(".ruleType select").hide();
            $(".ruleType select").prop('disabled', true);

        }
    }
    else
    {
        //Disable and hide input text
        $(".ruleType input:text").prop('disabled', true);
        $(".ruleType input:text").hide();

        $(".ruleType select").prop('disabled', false);
        $(".ruleType input:hidden").val($(".ruleType select").val());
        $(".ruleType select").show();
    }
}