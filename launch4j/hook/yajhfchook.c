#include "resource.h"
#include "head.h"
#include "hook.h"

#define YAJHFC_SUB_KEY "Software\\YajHFC"
#define BUFSIZE 255
#define WOW_KEY(x) ((x)|(wow64?KEY_WOW64_64KEY:0))

static char* ENV_VARS_TO_SAVE[] = {
       "TMP",
       "TEMP",
       "USERPROFILE",
       NULL       
};

static void appendArgsFromRegistry(char* args, const char* prefix) 
{
     HKEY yajKey;
     char name[BUFSIZE];
     char value[BUFSIZE];
     int i;
     DWORD valueSize, nameSize, type, rv;
     
     if ((rv=RegOpenKeyEx(HKEY_LOCAL_MACHINE, YAJHFC_SUB_KEY, 0, WOW_KEY(KEY_QUERY_VALUE), &yajKey)) != ERROR_SUCCESS) 
     {
       debug("appendArgsFromRegistry(%s):\t RegOpenKeyEx failed:\t%d\n", prefix, rv);
       return;
     }     
     
     valueSize=nameSize=BUFSIZE;
     for (i=0; RegEnumValue(yajKey, i, name, &nameSize, NULL, &type, value, &valueSize) == ERROR_SUCCESS; i++)
     {
         if (type==REG_SZ && strncmp(name, prefix, strlen(prefix))==0) 
         {
            strcat(args, value);
            strcat(args, " ");
         } else {
          debug("appendArgsFromRegistry(%s):\t ignored value %s has type %d\n", prefix, name, type);
        }
        valueSize=nameSize=BUFSIZE;
     }
     RegCloseKey(yajKey);
}

static void appendArgFromRegistry(char* args, const char* name) 
{
     HKEY yajKey;
     char value[BUFSIZE];
     int i;
     DWORD valueSize, type, rv;
     
     if ((rv = RegOpenKeyEx(HKEY_LOCAL_MACHINE, YAJHFC_SUB_KEY, 0, WOW_KEY(KEY_QUERY_VALUE), &yajKey)) != ERROR_SUCCESS) 
     {
       debug("appendArgFromRegistry(%s):\t RegOpenKeyEx failed:\t%d\n", name, rv);
       return;
     }     
     
     valueSize=BUFSIZE;
     if ((rv=RegQueryValueEx(yajKey, name, NULL, &type, value, &valueSize)) == ERROR_SUCCESS)
     {
        if (type==REG_SZ) {
               strcat(args, value);                   
               strcat(args, " ");
        } else {
          debug("appendArgFromRegistry(%s):\t value has type %d != REG_SZ\n", name, type);
        }
     } else {
        debug("appendArgFromRegistry(%s):\t RegQueryValueEx failed:\t%d\n", name, rv);
     }
     RegCloseKey(yajKey);
}

static void saveEnvVars() 
{
     HKEY yajKey;
     char value[BUFSIZE];
     int i;
     DWORD valueSize, type, rv;
     
     if ((rv=RegCreateKeyEx(HKEY_CURRENT_USER, YAJHFC_SUB_KEY, 0, NULL, REG_OPTION_NON_VOLATILE, WOW_KEY(KEY_SET_VALUE), NULL, &yajKey, NULL)) != ERROR_SUCCESS) 
     {
       debug("saveEnvVars:\t RegCreateKeyEx failed:\t%d\n", rv);
       return;
     }     
     
     char** var=ENV_VARS_TO_SAVE;
     do {
        valueSize=GetEnvironmentVariable(*var, value, BUFSIZE);
        if (valueSize>0) {
           if ((rv=RegSetValueEx(yajKey, *var, 0, REG_SZ, value, valueSize)) != ERROR_SUCCESS) {
              debug("saveEnvVars:\t RegSetValueEx failed for %s:\t%d\n", *var, rv);
           } else {
              debug("saveEnvVars:\t RegSetValueEx succeeded for %s.\n", *var);
           }
        }
     } while (*(++var)!=NULL);  
     RegCloseKey(yajKey);
}

static void loadSavedEnvVars() 
{
     HKEY yajKey;
     char value[BUFSIZE];
     int i;
     DWORD valueSize, type, rv;
     
     if ((rv=RegOpenKeyEx(HKEY_CURRENT_USER, YAJHFC_SUB_KEY, 0, WOW_KEY(KEY_QUERY_VALUE), &yajKey)) != ERROR_SUCCESS) 
     {
       debug("loadSavedEnvVars:\t RegOpenKeyEx failed:\t%d\n", rv);
       return;
     }     
     
     char** var=ENV_VARS_TO_SAVE;
     do {
        valueSize=BUFSIZE;
        if ((rv=RegQueryValueEx(yajKey, *var, NULL, &type, value, &valueSize)) == ERROR_SUCCESS)
        {
           if (type==REG_SZ) {
             SetEnvironmentVariable(*var, value);
             debug("loadSavedEnvVars:\t set %s=%s\n", *var, value);            
           } else {
             debug("loadSavedEnvVars:\t value %s has type %d != REG_SZ\n", *var, type);
           }
        } else {
           debug("loadSavedEnvVars:\t RegQueryValueEx failed for %s:\t%d\n", *var, rv);
        }
     } while (*(++var)!=NULL);  
     RegCloseKey(yajKey);
}

/*
 Allows you to set custom environment variables
*/
void setCustomEnvVars() 
{
     #ifdef PRINTLAUNCH
     loadSavedEnvVars();
     #endif
}

/*
  Allows you to set custom JVM options
*/
void appendCustomJVMOptions(char *args)
{
    appendArgsFromRegistry(args, "addJavaArg");
}


/*
  Allows you to append custom class path items
*/
void appendCustomClassPath(char *args)
{
     // Do nothing by default
}


static void str_replace(char* str, char to_replace, char replacement)
{
     do
     {
       if (*str == to_replace)
         *str = replacement;
     } while (*(++str));
}

/*
  Allows you to set custom JVM options
*/
void appendCustomCommandLineArgs(char *args)
{
     strcat(args, " ");
     
     #ifdef PRINTLAUNCH
     char buf[BUFSIZE];
     
     if (GetEnvironmentVariable("REDMON_DOCNAME", buf, BUFSIZE)>0)
     {
        str_replace(buf, '"', '\'');
        
        strcat(args, "--subject=\"");
        strcat(args, buf);
        strcat(args, "\" ");
     }
     appendArgFromRegistry(args, "printlaunchyajhfcparams");
     #endif
     appendArgsFromRegistry(args, "addLaunchArg");
}


/*
 Allows some more custom preparation just before launching the code
*/
void customPrepare(char* cmd, char* args) 
{
     #ifndef PRINTLAUNCH
          saveEnvVars();
     #endif
}
