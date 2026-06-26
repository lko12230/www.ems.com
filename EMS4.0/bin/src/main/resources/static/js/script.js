console.log("this is javascript file")
const toggleSidebar =() => {
	if($(".sidebar").is(":visible"))
	{
		$(".sidebar").css("display","none");
		$(".content").css("margin-left","0%");
		
	}
	else
	{
		$(".sidebar").css("display","block");
		$(".content").css("margin-left","21%");
	}
};

const paymentStart =() => {
	console.log("Payment Started..");
	var amount=$("#payment_field").val();
	console.log(amount);
	if(amount=="" || amount==null)
	{
		console.log("inside if block")
		alert("amount is required")
		return;
	}


$.ajax(
	{
		url:"/admin/create_order",
		data:JSON.stringify({amount: amount ,info : "order_request"}),
		contentType:"application/json",
		type:"POST",
		dataType:"json",
		success:function(response)
		{
			console.log(response)
			if(response.status=="created")
			{
				let options={
					key:"rzp_test_icIfOJXJUlRjph",
					amount:amount.response,
					currency:"INR",
					name:"EMS",
					description:"SUBSCRIPTION",
					image:"data:image/jpeg;base64,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",
					order_id:response.id,
					handler:function(response)
					{
						 console.log(response.razorpay_payment_id);
						 console.log(response.razorpay_order_id);
						 console.log(response.razorpay_signature);
						 updatePaymentOnServer(response.razorpay_payment_id,response.razorpay_order_id,"paid");
					},
					    prefill: 
					    {
						 name: "",
						 email: "",
						 contact: "",
						 },
						 notes: 
						 {
						 address: "",
						 },
						 theme:
						  {
						 color: "#3399cc",
						 }
				};
				let rzp = new Razorpay(options);
rzp.on('payment.failed', function (response){
 console.log(response.error.code);
 console.log(response.error.description);
 console.log(response.error.source);
 console.log(response.error.step);
 console.log(response.error.reason);
 console.log(response.error.metadata.order_id);
 console.log(response.error.metadata.payment_id);
 alert("PAYMENT FAILED!!")
 swal("Oops !", "PAYMENT FAILED!!", "error");
});
rzp.open();
			}
		},
		error:function(error)
		{
			console.log(error)
			swal("Something Went Wrong !", "PAYMENT CANNOT BE PROCCESS!!", "error");
		},
		
	});
};

 function updatePaymentOnServer(payment_id,order_id,status)
 {
	$.ajax({
		url:"/admin/update_order",
		data:JSON.stringify({payment_id: payment_id ,order_id : order_id,status : status}),
		contentType:"application/json",
		type:"POST",
		dataType:"json",
		success:function(response)
		{
				swal("Good job!", "PAYMENT SUCCESS !!", "success");
		},
		error:function(error) {
			 swal("Oops !", "PAYMENT PENDING!!", "warning");
		},
	}) ;
 }


/*function checkAuthenticationStatus() {
    fetch('/api/authenticated')
        .then(response => response.json())
        .then(data => {
            if (!data.authenticated) {
                // Session has expired or user is not authenticated
                // Redirect to login page or show appropriate message
                window.location.href = '/signin'; // Example redirect to login page
            }
        })
        .catch(error => {
            console.error('Error:', error);
        });
}

// Check authentication status periodically
setInterval(checkAuthenticationStatus, 1000); // Check every 5 minutes (adjust interval as needed)
*/