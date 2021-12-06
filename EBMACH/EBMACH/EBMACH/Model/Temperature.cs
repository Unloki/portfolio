using System;
using System.Collections.Generic;
using System.Text;

namespace EBMACH.Model
{
    public class Temperature
    {
        public float beer { get; set; }

        public float room { get; set; }

        public string date { get; set; }
        public void SetDate()
        {
            this.DateFormatObject = new DateFormat(this.date);
        }
        public DateFormat DateFormatObject { get; set; }
    }
}
