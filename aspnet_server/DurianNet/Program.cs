using DurianNet.Data;
using DurianNet.Hubs;
using DurianNet.Services.CommentService;
using DurianNet.Services.DetectionService;
using DurianNet.Services.DetectionService.YOLO.v10;
using DurianNet.Services.SellerService;
using Microsoft.EntityFrameworkCore;

var builder = WebApplication.CreateBuilder(args);

// Add services to the container.
builder.Services.AddControllersWithViews();

builder.Services.AddEndpointsApiExplorer();
builder.Services.AddSwaggerGen();

//Setup dependencies
builder.Services.AddSingleton<IDetector, YoloV10Detector>();
builder.Services.AddSingleton<ISellerService, FakeSellerService>();
builder.Services.AddSingleton<ICommentService, FakeCommentService>();


// Setup signalR
builder.Services.AddSignalR(option =>
{
    option.ClientTimeoutInterval = TimeSpan.FromMinutes(10);
    option.KeepAliveInterval = TimeSpan.FromSeconds(10);
    option.EnableDetailedErrors = true;
    option.MaximumReceiveMessageSize = null; // unlimited

}).AddMessagePackProtocol();

// Setup Database
builder.Services.AddDbContext<ApplicationDBContext>(options =>
{
    options.UseSqlServer(builder.Configuration.GetConnectionString("DefaultConnection"));
});

var app = builder.Build();

// Configure the HTTP request pipeline.
if (!app.Environment.IsDevelopment())
{
    app.UseExceptionHandler("/Home/Error");
    // The default HSTS value is 30 days. You may want to change this for production scenarios, see https://aka.ms/aspnetcore-hsts.
    app.UseHsts();
}
else
{
    app.UseSwagger();
    app.UseSwaggerUI();
}

app.UseHttpsRedirection();
app.UseStaticFiles();

app.UseRouting();

app.UseAuthorization();

app.MapControllerRoute(
    name: "default",
    pattern: "{controller=Home}/{action=Index}/{id?}");

app.MapHub<ObjectDetectionHub>("/DetectionHub");

app.Run();
