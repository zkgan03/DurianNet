using DurianNet.Data;
using DurianNet.Exceptions;
using DurianNet.Hubs;
using DurianNet.Interfaces;
using DurianNet.Models.DataModels;
using DurianNet.Repository;
using DurianNet.Services;
using DurianNet.Services.CommentService;
using DurianNet.Services.DetectionService;
using DurianNet.Services.DetectionService.YOLO.v10;
using DurianNet.Services.SellerService;
using Microsoft.AspNetCore.Authentication.JwtBearer;
using Microsoft.AspNetCore.Diagnostics;
using Microsoft.AspNetCore.Identity;
using Microsoft.EntityFrameworkCore;
using Microsoft.IdentityModel.Tokens;
using Microsoft.OpenApi.Models;
using System.Net;
using System.Text.Json;

var builder = WebApplication.CreateBuilder(args);

// Add services to the container.
builder.Services.AddControllersWithViews();

builder.Services.AddEndpointsApiExplorer();
builder.Services.AddSwaggerGen();

builder.Services.AddSwaggerGen(option =>
{
    option.SwaggerDoc("v1", new OpenApiInfo { Title = "Demo API", Version = "v1" });
    option.AddSecurityDefinition("Bearer", new OpenApiSecurityScheme
    {
        In = ParameterLocation.Header,
        Description = "Please enter a valid token",
        Name = "Authorization",
        Type = SecuritySchemeType.Http,
        BearerFormat = "JWT",
        Scheme = "Bearer"
    });
    option.AddSecurityRequirement(new OpenApiSecurityRequirement
    {
        {
            new OpenApiSecurityScheme
            {
                Reference = new OpenApiReference
                {
                    Type=ReferenceType.SecurityScheme,
                    Id="Bearer"
                }
            },
            new string[]{}
        }
    });
});

//Setup dependencies
builder.Services.AddSingleton<IDetector, YoloV10Detector>();
builder.Services.AddScoped<ISellerService, SellerService>();
builder.Services.AddScoped<ICommentService, CommentService>();
builder.Services.AddScoped<IDurianProfileRepository, DurianProfileRepository>();
builder.Services.AddScoped<IDurianVideoRepository, DurianVideoRepository>();
builder.Services.AddScoped<IUserRepository, UserRepository>();
builder.Services.AddScoped<ITokenService, TokenService>();
builder.Services.AddScoped<IFavoriteDurian,  FavoriteDurianRepository>();

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

builder.Services.AddIdentity<User, IdentityRole>(options =>
{
    options.Password.RequireDigit = true;
    options.Password.RequireLowercase = true;
    options.Password.RequireUppercase = true;
    options.Password.RequireNonAlphanumeric = true;
    options.Password.RequiredLength = 12;
})
.AddEntityFrameworkStores<ApplicationDBContext>();

builder.Services.AddAuthentication(options =>
{
    options.DefaultAuthenticateScheme =
    options.DefaultChallengeScheme =
    options.DefaultForbidScheme =
    options.DefaultScheme =
    options.DefaultSignInScheme =
    options.DefaultSignOutScheme = JwtBearerDefaults.AuthenticationScheme;
}).AddJwtBearer(options =>
{
    options.TokenValidationParameters = new TokenValidationParameters
    {
        ValidateIssuer = true,
        ValidIssuer = builder.Configuration["JWT:Issuer"],
        ValidateAudience = true,
        ValidAudience = builder.Configuration["JWT:Audience"],
        ValidateIssuerSigningKey = true,
        IssuerSigningKey = new SymmetricSecurityKey(
            System.Text.Encoding.UTF8.GetBytes(builder.Configuration["JWT:SigningKey"])
        )
    };
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

app.UseAuthentication();
app.UseAuthorization();

app.MapControllerRoute(
    name: "default",
    pattern: "{controller=Home}/{action=RedirectToLoginPage}/{id?}");


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