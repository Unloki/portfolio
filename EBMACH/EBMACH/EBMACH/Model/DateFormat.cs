using System;
using System.Collections.Generic;
using System.Text;

namespace EBMACH.Model
{
    public class DateFormat
    {
        public DateFormat(String inputDate)
        {
            Console.WriteLine("New Date Format object");
            string[] dateAll = inputDate.Split('|');
            if(dateAll.Length > 1)
            {
                Console.WriteLine("New Date Format case long");
                string[] dateDWM = dateAll[0].Split('-');
                string[] dateHours = dateAll[1].Split(':');

                this.Day = Int32.Parse(dateDWM[0]);
                this.Month = Int32.Parse(dateDWM[1]);
                this.Year = Int32.Parse(dateDWM[2]);
                this.Hour = Int32.Parse(dateHours[0]);
                this.Min = Int32.Parse(dateHours[1]);
                this.Second = Int32.Parse(dateHours[1]);
            }
            else if(dateAll.Length == 1)
            {
                Console.WriteLine("New Date Format case short");
                string[] dateDWM = dateAll[0].Split('-');

                this.Day = Int32.Parse(dateDWM[0]);
                this.Month = Int32.Parse(dateDWM[1]);
                this.Year = Int32.Parse(dateDWM[2]);
                this.Hour = 0;
                this.Min = 1;
                this.Second = 0;
            }
        }
        public int Month { get; set; }

        public int Year { get; set; }

        public int Day { get; set; }

        public int Hour { get; set; }

        public int Min { get; set; }
        public int Second { get; set; }

    }
}
