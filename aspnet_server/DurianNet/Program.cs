using DurianNet.Data;
using DurianNet.Exceptions;
using DurianNet.Hubs;
using DurianNet.Models.DataModels;
using DurianNet.Services.Chatbot;
using DurianNet.Services.CommentService;
using DurianNet.Services.DetectionService;
using DurianNet.Services.DetectionService.YOLO.v10;
using DurianNet.Services.DurianVideoService;
using DurianNet.Services.FavoriteDurianService;
using DurianNet.Services.DetectionService.YOLO.v8;
using DurianNet.Services.SellerService;
using DurianNet.Services.TokenService;
using DurianNet.Services.UserService;
using Microsoft.AspNetCore.Authentication.Cookies;
using Microsoft.AspNetCore.Authentication.JwtBearer;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Diagnostics;
using Microsoft.AspNetCore.Identity;
using Microsoft.EntityFrameworkCore;
using Microsoft.IdentityModel.Tokens;
using Microsoft.OpenApi.Models;
using System.Net;
using System.Text;
using System.Text.Json;

using System.Security.Claims;

using DurianNet.Services.DurianProfileService;


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
builder.Services.AddSingleton<IDetector, YoloV8Detector>();
builder.Services.AddScoped<ISellerService, SellerService>();
builder.Services.AddScoped<ICommentService, CommentService>();
builder.Services.AddScoped<IDurianProfileRepository, DurianProfileRepository>();
builder.Services.AddScoped<IDurianVideoRepository, DurianVideoRepository>();
builder.Services.AddScoped<IUserRepository, UserRepository>();
builder.Services.AddScoped<ITokenService, TokenService>();
builder.Services.AddScoped<IFavoriteDurian, FavoriteDurianRepository>();
builder.Services.AddScoped<IChatbotService, ChatbotService>();

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
    options.Password.RequiredLength = 8;
})
.AddEntityFrameworkStores<ApplicationDBContext>()
.AddDefaultTokenProviders();

builder.Services.AddAuthorization(options =>
{

    options.DefaultPolicy = new AuthorizationPolicyBuilder(JwtBearerDefaults.AuthenticationScheme)
        .RequireAuthenticatedUser()
        .Build();

    // Policy for Admins and SuperAdmins
    options.AddPolicy("AdminPolicy", policy =>
    {
        policy.AddAuthenticationSchemes(CookieAuthenticationDefaults.AuthenticationScheme);
        policy.RequireAuthenticatedUser();
        policy.RequireClaim(ClaimTypes.Role, UserType.Admin.ToString(), UserType.SuperAdmin.ToString());
        //policy.RequireRole("Admin");
    });

    //Policy for SuperAdmin only
    options.AddPolicy("SuperAdminPolicy", policy =>
    {
        policy.AddAuthenticationSchemes(CookieAuthenticationDefaults.AuthenticationScheme);
        policy.RequireAuthenticatedUser();
        policy.RequireClaim(ClaimTypes.Role, UserType.SuperAdmin.ToString()); // Only allow SuperAdmin
    });
});

/*builder.Services.AddAuthentication(JwtBearerDefaults.AuthenticationScheme)
    .AddCookie(CookieAuthenticationDefaults.AuthenticationScheme, options =>
    {
        options.LoginPath = "/Account/LoginPage";
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
                Encoding.UTF8.GetBytes(builder.Configuration["JWT:SigningKey"])
            )
        };
    });*/

builder.Services.AddAuthentication(options =>
{
    // Supports multiple authentication schemes
    options.DefaultScheme = CookieAuthenticationDefaults.AuthenticationScheme; // Default for MVC views (admin)
    options.DefaultChallengeScheme = JwtBearerDefaults.AuthenticationScheme; // Default for API endpoints
})
.AddCookie(CookieAuthenticationDefaults.AuthenticationScheme, options =>
{
    options.LoginPath = "/Account/LoginPage"; // Redirect to login page if unauthenticated
    options.AccessDeniedPath = "/Account/AccessDenied"; // Redirect to access denied page
    //options.ExpireTimeSpan = TimeSpan.FromMinutes(30); // Session timeout
    //options.SlidingExpiration = true; // Extend session if active
})
.AddJwtBearer(options =>
{
    options.TokenValidationParameters = new TokenValidationParameters
    {
        ValidateLifetime = false,
        ValidateIssuer = true,
        ValidIssuer = builder.Configuration["JWT:Issuer"],
        ValidateAudience = true,
        ValidAudience = builder.Configuration["JWT:Audience"],
        ValidateIssuerSigningKey = true,
        IssuerSigningKey = new SymmetricSecurityKey(
            Encoding.UTF8.GetBytes(builder.Configuration["JWT:Key"])
        )
    };
});


// Enable session services
builder.Services.AddDistributedMemoryCache();
builder.Services.AddSession(options =>
{
    options.Cookie.Name = ".DurianNet.Session";
    //options.IdleTimeout = TimeSpan.FromMinutes(30); // Session timeout
    options.Cookie.HttpOnly = true; // Ensures session cookie is accessible only via HTTP
    options.Cookie.IsEssential = true; // Mark the session cookie as essential
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

// Enable session middleware
app.UseSession();

app.UseRouting();

app.UseAuthentication();
app.UseAuthorization();

app.MapControllerRoute(
    name: "default",
    pattern: "{controller=Home}/{action=RedirectToLoginPage}/{id?}");


app.MapHub<ObjectDetectionHub>("/DetectionHub");

// Seed database if in development mode
if (app.Environment.IsDevelopment())
{
    SeedDatabase(app);
}

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