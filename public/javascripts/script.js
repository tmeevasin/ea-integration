$(function(){
   $('body').on('click', '.jQtoggleView', function(){
       var $trigger = $(this);
       var $root = $trigger.parents('.jQtoggleView_root');
       var $target = $root.find('.jQtoggleView_target');
       
       if ($trigger.hasClass('open')) {
           $trigger.removeClass('open');
           $target.removeClass('open').slideUp(300);           
       }else{
           $trigger.addClass('open');
           $target.addClass('open').slideDown(300);
       }
   }) 
});