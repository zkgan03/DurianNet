using DurianNet.Data;
using DurianNet.Exceptions;
using DurianNet.Hubs;
using DurianNet.Services.CommentService;
using DurianNet.Services.DetectionService;
using DurianNet.Services.DetectionService.YOLO.v10;
using DurianNet.Services.SellerService;
using Microsoft.AspNetCore.Diagnostics;
using Microsoft.EntityFrameworkCore;
using System.Net;
using System.Text.Json;

var builder = WebApplication.CreateBuilder(args);

// Add services to the container.
builder.Services.AddControllersWithViews();

builder.Services.AddEndpointsApiExplorer();
builder.Services.AddSwaggerGen();

//Setup dependencies
builder.Services.AddSingleton<IDetector, YoloV10Detector>();
builder.Services.AddScoped<ISellerService, SellerService>();
builder.Services.AddScoped<ICommentService, CommentService>();


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

// Middleware to handle exceptions for API
app.UseWhen(context => context.Request.Path.StartsWithSegments("/api"), subApp =>
{
    subApp.UseExceptionHandler(builder =>
    {
        builder.Run(async context =>
        {
            var exceptionHandlerPathFeature = context.Features.Get<IExceptionHandlerPathFeature>();
            if (exceptionHandlerPathFeature?.Error is DataNotFoundException dataNotFoundException)
            {
                context.Response.StatusCode = (int)HttpStatusCode.NotFound;
                await context.Response.WriteAsync(dataNotFoundException.Message);
            }
            else
            {
                context.Response.StatusCode = (int)HttpStatusCode.InternalServerError;
                await context.Response.WriteAsync("An unexpected fault happened. Try again later.");
            }
        });
    });
});

app.UseHttpsRedirection();
app.UseStaticFiles();

app.UseRouting();

app.UseAuthorization();

app.MapControllerRoute(
    name: "default",
    pattern: "{controller=Home}/{action=Index}/{id?}");

app.MapHub<ObjectDetectionHub>("/DetectionHub");

// Seed database
SeedDatabase(app); // TODO : Remove this method

app.Run();


// TODO : Remove this method
// Method to initialize and seed the database
void SeedDatabase(IHost app)
{
    using (var scope = app.Services.CreateScope())
    {
        var services = scope.ServiceProvider;
        var context = services.GetRequiredService<ApplicationDBContext>();

        // Call your DbInitializer
        DbInitializer.Seed(context);
    }
}